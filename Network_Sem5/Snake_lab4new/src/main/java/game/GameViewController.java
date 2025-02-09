package game;

import io.PlayerController;

public class GameViewController {
    public GameViewController(PlayerController playerController, GameView gameView) {
        playerController.getNewMessageSubject().subscribe(messageWithSender -> {
            if (messageWithSender.getMessage().hasState()) {
                gameView.setState(messageWithSender.getMessage().getState().getState());
            }
        });
    }
}