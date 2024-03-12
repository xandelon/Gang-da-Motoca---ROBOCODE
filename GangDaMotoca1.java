package NOSSOROBO;
import robocode.*;
import java.awt.Color;

public class GangDaMotoca1 extends AdvancedRobot {
  int gunDirection = 1;

  public void run() {
    // Mudança de cor
    setBodyColor(Color.black);
    setRadarColor(Color.cyan);
    setGunColor(Color.black);
    setBulletColor(Color.red);

    // Gira a arma infinitamente ate achar um inimigo
    while (true) {
      turnGunRight(360);
    }
  }

  public void onScannedRobot(ScannedRobotEvent e) {
    // Vira o robô em direção ao inimigo
    setTurnRight(e.getBearing());
    // Atira sempre que ta olhando pra um inimigo
    setFire(2);
    // E anda pra frente
    setAhead(100);
    // Inverte a direção da arma a cada turno
    gunDirection = -gunDirection;
    // Vira em 360 graus (sentido horario ou anti-horario)
    setTurnGunRight(360 * gunDirection);
    // Executa todas as ações pendentes
    execute();
  }
}
