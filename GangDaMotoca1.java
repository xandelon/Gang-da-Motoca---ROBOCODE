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
    // MudanÃ§a de cor
    setBodyColor(Color.black);
    setRadarColor(Color.cyan);
    setGunColor(Color.black);
    setBulletColor(Color.cyan);

    // Gira a arma e o radar infinitamente ate achar um inimigo
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);

    // Movimento padrÃ£o
    while (true) {
      turnRadarRightRadians(Double.MAX_VALUE); // Movimento de radar padrÃ£o
      execute();
    }
  }

  public void onScannedRobot(ScannedRobotEvent e) {
    // Vira o robÃ´ em direÃ§Ã£o ao inimigo
    double absBearing = e.getBearingRadians() + getHeadingRadians();
    setTurnRightRadians(Utils.normalRelativeAngle(absBearing - getHeadingRadians() + Math.PI / 2 - moveDirection * Math.PI / 4));

    // Vira a arma para o inimigo
    setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));

    // Atira baseado na energia do inimigo e na distÃ¢ncia
    double firePower = Math.min(500 / e.getDistance(), e.getEnergy() / 4);
    setFire(firePower);

    // Detecta a perda de energia do inimigo
    double energyDrop = enemyEnergy - e.getEnergy();
    if (energyDrop > 0 && energyDrop <= 3) {
      // Se o inimigo disparou uma bala, muda de direÃ§Ã£o
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

    // Movimento oscilatÃ³rio
    if (e.getDistance() > 150) {
        setAhead((e.getDistance() / 4 + 25) * moveDirection);
    } else {
        setBack((e.getDistance() / 4 + 25) * moveDirection);
    }

    // Executa todas as aÃ§Ãµes pendentes
    execute();
  }

  public void onHitWall(HitWallEvent e) {
  // Inverte a direÃ§Ã£o do movimento
  moveDirection = -moveDirection;

  // Vira o robÃ´ para longe da parede
  setTurnRight(180 + random.nextInt(180) - 90); // Adiciona um Ã¢ngulo aleatÃ³rio entre -90 e 90 graus

  // Move o robÃ´ para longe da parede
  setAhead((100 + random.nextInt(200)) * moveDirection); // Adiciona uma distÃ¢ncia aleatÃ³ria entre 100 e 300

  // Executa todas as aÃ§Ãµes pendentes
  execute();
	}
}
