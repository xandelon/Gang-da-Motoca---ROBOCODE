package GDM;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.util.Random;

public class GangueDaMotoca extends AdvancedRobot {
  int moveDirection = 1;
  double enemyEnergy = 100;
  Random random = new Random();

  public void run() {
    // Mudança de cor
    setBodyColor(Color.black);
    setRadarColor(Color.cyan);
    setGunColor(Color.black);
    setBulletColor(Color.cyan);

    // Gira a arma e o radar infinitamente ate achar um inimigo
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);

    // Movimento padrÃ£o
    while (true) {
      // Verifica se o robo está¡ perto de uma parede
      if (getX() <= 50 || getY() <= 50 || getBattleFieldWidth() - getX() <= 50 || getBattleFieldHeight() - getY() <= 50) {
        // Se estiver perto de uma parede, vira 180 graus e move na direÃ§Ã£o oposta
        moveDirection = -moveDirection;
        setTurnRight(180);
        setAhead(100 * moveDirection);
      } else {
        setAhead(100 * moveDirection);
        setTurnRight(90);
      }
      turnRadarRightRadians(Double.MAX_VALUE); // Movimento de radar padrão
      execute();
    }
  }

  public void onScannedRobot(ScannedRobotEvent e) {
    
    // Vira o robo em direçãoo ao inimigo
    double absBearing = e.getBearingRadians() + getHeadingRadians();
    setTurnRightRadians(Utils.normalRelativeAngle(absBearing - getHeadingRadians() + Math.PI / 2 - moveDirection * Math.PI / 4));

    // Vira a arma para o inimigo
    setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));

    // Atira baseado na energia do inimigo e na distancia
    double firePower = Math.min(500 / e.getDistance(), e.getEnergy() / 4);
    setFire(firePower);

    // Detecta a perda de energia do inimigo
    double energyDrop = enemyEnergy - e.getEnergy();
    if (energyDrop > 0 && energyDrop <= 3) {
      // Se o inimigo disparou uma bala, muda de direção
      moveDirection = -moveDirection;
      setAhead(50 * moveDirection);
    }
    enemyEnergy = e.getEnergy();

    // Rastreia o inimigo com o radar
    double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
    double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
    if (radarTurn < 0)
      radarTurn -= extraTurn;
    else
      radarTurn += extraTurn;
    setTurnRadarRightRadians(radarTurn);

    // Executa todas as açoes pendentes
    execute();
  }

  public void onHitWall(HitWallEvent e) {
    // Inverte a direção do movimento
    moveDirection = -moveDirection;
  
    // Vira o robo para longe da parede
    setTurnRight(180);
  
    // Move o robo para longe da parede
    setAhead(100 * moveDirection);
  
    // Executa todas as ações pendentes
    execute();
  }
}
