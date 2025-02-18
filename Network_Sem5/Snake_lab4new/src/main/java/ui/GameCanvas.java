package ui;

import io.reactivex.rxjava3.subjects.Subject;
import proto.SnakesProto;
import game.GameView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Function;

public class GameCanvas extends JPanel implements GameView {
    private final int canvasWidth;
    private final int canvasHeight;

    private SnakesProto.GameState state;

    private static final Color FOOD_COLOR = Color.RED;

    private static final Color[] SNAKE_COLORS = {
            Color.BLUE,
            Color.YELLOW,
            Color.LIGHT_GRAY,
            Color.GREEN,
            Color.CYAN,
            Color.ORANGE,
            Color.MAGENTA,
            Color.PINK,
    };

    public GameCanvas(Subject<Control> controlSubject) {
        canvasWidth = 405;
        canvasHeight = 305;
        setSize(canvasWidth, canvasHeight);
        setPreferredSize(new Dimension(canvasWidth, canvasHeight));

        setFocusable(true);
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "down");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "left");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "right");
        Function<SnakesProto.Direction, AbstractAction> actionFactory = direction -> new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlSubject.onNext(new Control(null, direction));
            }
        };

        actionMap.put("up", actionFactory.apply(SnakesProto.Direction.UP));
        actionMap.put("down", actionFactory.apply(SnakesProto.Direction.DOWN));
        actionMap.put("left", actionFactory.apply(SnakesProto.Direction.LEFT));
        actionMap.put("right", actionFactory.apply(SnakesProto.Direction.RIGHT));
    }

    @Override
    public void paint(Graphics g) {
        grabFocus();
        super.paint(g);
        drawState((Graphics2D) g);
    }

    private void drawState(Graphics2D canvas) {
        if (state == null) {
            return;
        }

        int width = state.getConfig().getWidth();
        int height = state.getConfig().getHeight();

        int cellWidth = canvasWidth / width;
        int cellHeight = canvasHeight / height;

        canvas.clearRect(0, 0, canvasWidth, canvasHeight);
        canvas.setStroke(new BasicStroke(0.5f));
        canvas.setColor(Color.BLACK);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                canvas.drawRect(i * cellWidth, j * cellHeight, cellWidth, cellHeight);
            }
        }

        for (var snake : state.getSnakesList()) {
            canvas.setStroke(new BasicStroke(4f));
            var firstPoint = true;
            int x = 0, y = 0;
            for (var point : snake.getPointsList()) {
                var ax = x + point.getX();
                var ay = y + point.getY();

                if (firstPoint) {
                    canvas.setColor(SNAKE_COLORS[snake.getPlayerId() % SNAKE_COLORS.length].darker());
                    firstPoint = false;
                } else {
                    canvas.setColor(SNAKE_COLORS[snake.getPlayerId() % SNAKE_COLORS.length]);
                }
                canvas.fillRect(ax * cellWidth, ay * cellHeight, cellWidth, cellHeight);

                x = ax;
                y = ay;
            }
        }

        canvas.setColor(FOOD_COLOR);
        for (var food: state.getFoodsList()) {
            canvas.fillRect(food.getX() * cellWidth, food.getY() * cellHeight, cellWidth, cellHeight);
        }
    }

    @Override
    public void setState(SnakesProto.GameState state) {
        this.state = state;
        repaint();
    }
}