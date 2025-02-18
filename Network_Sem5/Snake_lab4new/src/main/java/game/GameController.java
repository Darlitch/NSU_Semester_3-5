package game;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.rxjava3.core.Observable;
import proto.SnakesProto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GameController {

    private final Map<Integer, SnakesProto.Direction> steerChoices;

    private final Consumer<SnakesProto.GamePlayer> killPlayer;

    public GameController(Observable<GameView.Control> controlObservable, Consumer<SnakesProto.GamePlayer> killPlayer) {
        steerChoices = new ConcurrentHashMap<>();
        this.killPlayer = killPlayer;
        controlObservable.subscribe(control -> steerChoices.put(control.getPlayerId(), control.getDirection()));
    }

    private static SnakesProto.GameState.Coord movePoint(SnakesProto.GameState.Coord oldPoint, SnakesProto.Direction direction) {
        return switch (direction) {
            case UP -> SnakesProto.GameState.Coord.newBuilder().setX(oldPoint.getX()).setY(oldPoint.getY() - 1).build();
            case DOWN -> SnakesProto.GameState.Coord.newBuilder().setX(oldPoint.getX()).setY(oldPoint.getY() + 1).build();
            case LEFT -> SnakesProto.GameState.Coord.newBuilder().setX(oldPoint.getX() - 1).setY(oldPoint.getY()).build();
            case RIGHT -> SnakesProto.GameState.Coord.newBuilder().setX(oldPoint.getX() + 1).setY(oldPoint.getY()).build();
        };
    }

    public static SnakesProto.GameState.Coord normalizeCoord(SnakesProto.GameState.Coord coord, int width, int height) {
        return SnakesProto.GameState.Coord.newBuilder()
                .setX((coord.getX() + width) % width)
                .setY((coord.getY() + height) % height)
                .build();
    }

    public SnakesProto.GameState.Snake moveSnake(SnakesProto.GameState.Snake oldSnake, SnakesProto.Direction direction, int width, int height, boolean eaten) {
        var newDirection = canSteer(oldSnake.getHeadDirection(), direction) ? direction : oldSnake.getHeadDirection();
        var newSnake = SnakesProto.GameState.Snake.newBuilder(oldSnake);
        newSnake.setHeadDirection(newDirection);
        newSnake.clearPoints();
        var newPoint = movePoint(oldSnake.getPoints(0), newDirection);
        newPoint = normalizeCoord(newPoint, width, height);
        newSnake.addPoints(newPoint);
        newSnake.addPoints(
                SnakesProto.GameState.Coord.newBuilder()
                        .setX(oldSnake.getPoints(0).getX() - newPoint.getX())
                        .setY(oldSnake.getPoints(0).getY() - newPoint.getY())
                        .build()
        );
        for (var idx = 1; idx < oldSnake.getPointsCount() - (eaten ? 0 : 1); idx++) {
            newSnake.addPoints(oldSnake.getPoints(idx));
        }
        return newSnake.build();
    }

    private static boolean canSteer(SnakesProto.Direction a, SnakesProto.Direction b) {
        return switch (a) {
            case UP -> switch (b) {
                case DOWN -> false;
                default -> true;
            };
            case DOWN -> switch (b) {
                case UP -> false;
                default -> true;
            };
            case LEFT -> switch (b) {
                case RIGHT -> false;
                default -> true;
            };
            case RIGHT -> switch (b) {
                case LEFT -> false;
                default -> true;
            };
        };
    }

    private void iterateSnake(SnakesProto.GameState.Snake snake, BiConsumer<SnakesProto.GameState.Coord, Boolean> callback) {
        int x = 0, y = 0;
        var firstPoint = true;
        for (var point : snake.getPointsList()) {
            var ax = x + point.getX();
            var ay = y + point.getY();

            var coord = SnakesProto.GameState.Coord.newBuilder()
                    .setX(ax)
                    .setY(ay)
                    .build();

            callback.accept(coord, firstPoint);

            x = ax;
            y = ay;

            firstPoint = false;
        }
    }

    public SnakesProto.GameState getNextState(SnakesProto.GameState oldState) {
        var newStateBuilder = SnakesProto.GameState.newBuilder(oldState);

        var players = oldState.getPlayers().getPlayersList().stream().collect(
                Collectors.toMap(
                        SnakesProto.GamePlayer::getId,
                        e -> e
                )
        );
        var snakes = oldState.getSnakesList().stream().collect(
                Collectors.toMap(
                        SnakesProto.GameState.Snake::getPlayerId,
                        e -> e
                )
        );

        for (var playerId : players.keySet()) {
            if (!snakes.containsKey(playerId)) {
                var player = players.get(playerId);
                if (player.getRole() != SnakesProto.NodeRole.VIEWER) {
                    snakes.put(playerId,
                            SnakesProto.GameState.Snake.newBuilder()
                                    .addPoints(
                                            SnakesProto.GameState.Coord.newBuilder()
                                                    .setX(player.getId())
                                                    .setY(player.getId())
                                                    .build()
                                    )
                                    .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE)
                                    .setHeadDirection(steerChoices.getOrDefault(player.getId(), SnakesProto.Direction.DOWN))
                                    .setPlayerId(player.getId())
                                    .build()
                    );
                }
            }
        }

        var allFoods = new HashSet<>(oldState.getFoodsList());
        for (var entry : snakes.entrySet()) {
            var snake = entry.getValue();
            var eatenFood = oldState.getFoodsList().stream().filter(food -> food.equals(snake.getPoints(0))).findAny();
            entry.setValue(moveSnake(snake,
                    steerChoices.getOrDefault(snake.getPlayerId(), snake.getHeadDirection()),
                    oldState.getConfig().getWidth(),
                    oldState.getConfig().getHeight(),
                    eatenFood.isPresent()
            ));
            eatenFood.ifPresent(food -> {
                allFoods.remove(food);
                players.compute(snake.getPlayerId(), (k, player) -> SnakesProto.GamePlayer.newBuilder(player).setScore(player.getScore() + 10).build());
            });
        }

        HashMap<Integer, SnakesProto.GameState.Snake> deadSnakes = new HashMap<>();
        Multimap<SnakesProto.GameState.Coord, Integer> map = ArrayListMultimap.create();

        for (var entry : snakes.entrySet()) {
            var snake = entry.getValue();
            var playerId = entry.getKey();
            iterateSnake(snake, (coord, isHead) -> {
                if (isHead) {
                    var whoKilledId = map.get(coord);
                    if (!whoKilledId.isEmpty()) {
                        if (players.get(playerId).getRole() != SnakesProto.NodeRole.MASTER) {
                            killPlayer.accept(players.get(entry.getKey()));
                        }
                        deadSnakes.put(playerId, snake);
                        entry.setValue(null);
                        return;
                    }
                }

                map.put(coord, playerId);
            });
        }
        snakes.values().removeIf(Objects::isNull);
        for (var entry : snakes.entrySet()) {
            var who = map.get(entry.getValue().getPoints(0));
            if (who.size() > 1) {
                if (players.get(entry.getKey()).getRole() != SnakesProto.NodeRole.MASTER) {
                    killPlayer.accept(players.get(entry.getKey()));
                }
                deadSnakes.put(entry.getKey(), entry.getValue());
                entry.setValue(null);
            }
        }
        snakes.values().removeIf(Objects::isNull);
        newStateBuilder.clearSnakes();
        for (var snake : snakes.values()) {
            newStateBuilder.addSnakes(snake);
        }

        for (var deadSnake : deadSnakes.values()) {
            iterateSnake(deadSnake, (coord, isHead) -> {
                if (ThreadLocalRandom.current().nextInt(0, 100) < oldState.getConfig().getDeadFoodProb() * 100) {
                    allFoods.add(coord);
                }
            });
        }
        var newPlayers = SnakesProto.GamePlayers.newBuilder();
        for (var player : players.values()) {
            if (deadSnakes.containsKey(player.getId()) && player.getRole() != SnakesProto.NodeRole.MASTER) {
                newPlayers.addPlayers(SnakesProto.GamePlayer.newBuilder(player).setRole(SnakesProto.NodeRole.VIEWER).build());
            } else {
                newPlayers.addPlayers(player);
            }
        }
        newStateBuilder.setPlayers(newPlayers);

        newStateBuilder.clearFoods();
        for (var food : allFoods) {
            newStateBuilder.addFoods(food);
        }
        var foodCount = oldState.getConfig().getFoodStatic() + (oldState.getConfig().getFoodPerPlayer() * snakes.size());
        for (var i = 0; i < foodCount - allFoods.size(); i++) {
            newStateBuilder.addFoods(SnakesProto.GameState.Coord.newBuilder()
                    .setX(ThreadLocalRandom.current().nextInt(0, oldState.getConfig().getWidth()))
                    .setY(ThreadLocalRandom.current().nextInt(0, oldState.getConfig().getHeight()))
                    .build()
            );
        }


        return newStateBuilder.build();
    }
}