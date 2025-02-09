package main

import (
	"encoding/binary"
	"fmt"
	"io"
	"net"
	"os"
	"runtime"
	"strconv"
)

const (
	SOCKS5 = 0x05 // Версия протокола SOCKS 5

	NO_AUTH = 0x00 // Без аутенфикации

	TCP_CONN_CMD = 0x01 // Команда установки TCP-соединения

	// Коды ответов SOCKS5
	SUCCEEDED            = 0x00 // Успех
	SERVER_FALTURE       = 0x01 // Ошибка сервера
	CONN_NOT_ALLOWED     = 0x02 // Соединение запрещено
	NET_UNREACHABLE      = 0x03 // Сеть недоступна
	HOST_UNREACHABLE     = 0x04 // Хост недоступен
	CONN_REFUSED         = 0x05 // Соединение отклонено
	TTL_ERR              = 0x06 // Ошибка TTL
	COMMAND_NOT_SUPP     = 0x07 // Команда не поддерживается
	ADDRES_TYPE_NOT_SUPP = 0x08 // Тип адреса не поддерживается
	METHOD_AUTH_NOT_SUPP = 0xFF // Метод аутенфикации не поддерживается

	IPV4 = 0x01
	DNS  = 0x03
	IPV6 = 0x04

	RSV = 0x00
)

func errMsg(err byte) []byte {
	return []byte{SOCKS5, err} //, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
}

func main() {
	runtime.GOMAXPROCS(1)

	port, err := strconv.Atoi(os.Args[1])
	if nil != err {
		fmt.Println(err.Error())
		return
	}

	addr := net.TCPAddr{
		IP:   []byte{},
		Port: port,
		Zone: "",
	}

	listener, err := net.ListenTCP("tcp", &addr)
	if nil != err {
		fmt.Println(err.Error())
		return
	}

	for {
		defer func() {
			if r := recover(); r != nil {
				fmt.Println("Восстановление после паники:", r)
			}
		}()
		conn, err := listener.AcceptTCP()
		if nil != err {
			fmt.Println(err.Error())
			return
		}

		go newConn(conn)
	}
}

func readNBytes(count int, conn *net.TCPConn) []byte {
	buffer := make([]byte, count)
	totalRead := 0
	for totalRead < count {
		n, err := io.ReadFull(conn, buffer)
		//n, err := conn.Read(buffer)
		if err != nil {
			fmt.Println(err.Error())
			return nil
		}
		totalRead += n
	}
	return buffer
}

func newConn(conn *net.TCPConn) {
	buffer := readNBytes(2, conn)
	if nil == buffer || len(buffer) == 0 {
		conn.Close()
		return
	}

	// Проверка на версию протокола
	if buffer[0] != SOCKS5 {
		_, err := conn.Write(errMsg(METHOD_AUTH_NOT_SUPP))
		if err != nil {
			fmt.Println("Ошибка при записи данных:", err)
			conn.Close()

		}
		conn.Close()
		return
	}

	var countMethods int = int(buffer[1])

	buffer = readNBytes(countMethods, conn)
	if nil == buffer {
		conn.Close()
		return
	}

	_, err := conn.Write(errMsg(NO_AUTH))
	if err != nil {
		fmt.Println("Ошибка при записи данных:", err)
		conn.Close()
		return
	}

	var serverAddr *net.TCPAddr
	flagRepeat := true
	for flagRepeat {
		flagRepeat = false

		buffer = readNBytes(4, conn)
		if nil == buffer {
			conn.Close()
			return
		}

		// Проверка пришла ли команда на установку TCP-соединения
		if buffer[1] != TCP_CONN_CMD {
			_, err := conn.Write(errMsg(COMMAND_NOT_SUPP))
			if err != nil {
				fmt.Println("Ошибка при записи данных:", err)
				conn.Close()
				return
			}
			flagRepeat = true
			continue
		}

		// Обрабатываем тип адреса
		switch buffer[3] {
		case IPV4:
			{
				buffer = readNBytes(6, conn) // Читаем 6 байт (4 байта IP и 2 байта порта)
				if nil == buffer {
					conn.Close()
					return
				}

				serverAddr = &net.TCPAddr{
					IP:   buffer[0:4],
					Port: int(binary.BigEndian.Uint16(buffer[4:])),
					Zone: "",
				} // Структура адреса целевого сервера

			}
		case DNS:
			{
				buffer = readNBytes(1, conn) // Читаем длину доменного имени
				if nil == buffer {
					conn.Close()
					return
				}

				dns := readNBytes(int(buffer[0]), conn)
				fmt.Println("dns", dns, string(dns))
				dnsString := string(dns)

				ip, err := net.LookupIP(dnsString)

				if err != nil {
					fmt.Println(err)
					flagRepeat = true
					continue
				}

				port := readNBytes(2, conn) // читаем порт

				serverAddr = &net.TCPAddr{
					IP:   ip[0],
					Port: int(binary.BigEndian.Uint16(port[:])),
					Zone: "",
				} // Структура адреса целевого сервера

				buffer = ip[0]
				buffer = append(buffer, port...)

			}

		case IPV6:
			{
				_, err := conn.Write([]byte{SOCKS5, ADDRES_TYPE_NOT_SUPP})
				if err != nil {
					fmt.Println("Ошибка при записи данных:", err)
					return
				}
				flagRepeat = true
				continue
			}
		default:
			{
				_, err := conn.Write([]byte{SOCKS5, SERVER_FALTURE})
				if err != nil {
					fmt.Println("Ошибка при записи данных:", err)
					return
				}
				flagRepeat = true
				continue
			}
		}
	}

	serverConn, err := net.DialTCP("tcp", nil, serverAddr) // устанавливаем соединение

	if nil != err {
		_, err := conn.Write(errMsg(NET_UNREACHABLE))
		if err != nil {
			fmt.Println("Ошибка при записи данных:", err)
			conn.Close()
			return
		}
		flagRepeat = true
	}

	_, err = conn.Write([]byte{SOCKS5, SUCCEEDED, RSV, IPV4, buffer[0], buffer[1], buffer[2], buffer[3], buffer[4], buffer[5]})
	if err != nil {
		fmt.Println("Ошибка при записи данных:", err)
		conn.Close()
		(*serverConn).Close()
		return
	}

	go cmdConnect(conn, serverConn)

}

func cmdConnect(client, server *net.TCPConn) {
	defer server.Close()
	defer client.Close()

	fmt.Println("!!!!!!!!!!!!!!!!!!!!!!")

	c1, c2 := make(chan struct{}), make(chan struct{}) // Создаём каналы для синхронизации

	// Горутина для передачи данных от клиента к серверу
	go func() {
		_, err := io.Copy(server, client)
		if err != nil {
			//client.Close()
		} else {
			server.CloseWrite()
		}
		close(c1)
	}()

	// Горутина для передачи данных от сервера к клиенту
	go func() {
		_, err := io.Copy(client, server)
		if err != nil {
			//server.Close()
		} else {
			client.CloseWrite()
		}
		close(c2)
	}()

	// Ожидаем завершения горутин
	select {
	case <-c1:
	}
	select {
	case <-c2:
	}
}
