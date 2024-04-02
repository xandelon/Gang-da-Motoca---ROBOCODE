package GDM;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Random;

public class GangueDaMotoca extends AdvancedRobot {
   int moveDirection = 1;
  double enemyEnergy = 100;
  Random random = new Random();

  public void run() {
    // MudanÃ§a de cor
    setBodyColor(Color.black);
    setRadarColor(Color.black);
    setGunColor(Color.red);
    setBulletColor(Color.red);

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

    // PrevisÃ£o de movimento linear
    double bulletPower = Math.min(500 / e.getDistance(), 3.0);
    double myX = getX();
    double myY = getY();
    double enemyX = getX() + e.getDistance() * Math.sin(absBearing);
    double enemyY = getY() + e.getDistance() * Math.cos(absBearing);
    double enemyHeading = e.getHeadingRadians();
    double enemyVelocity = e.getVelocity();

    double deltaTime = 0;
    double battleFieldHeight = getBattleFieldHeight(), 
         battleFieldWidth = getBattleFieldWidth();
    double predictedX = enemyX, predictedY = enemyY;
    while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
        Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
      predictedX += Math.sin(enemyHeading) * enemyVelocity;	
      predictedY += Math.cos(enemyHeading) * enemyVelocity;
      if(	predictedX < 18.0 
          || predictedY < 18.0
          || predictedX > battleFieldWidth - 18.0
          || predictedY > battleFieldHeight - 18.0){
        predictedX = Math.min(Math.max(18.0, predictedX), 
              battleFieldWidth - 18.0);	
        predictedY = Math.min(Math.max(18.0, predictedY), 
              battleFieldHeight - 18.0);
        break;
      }
    }
    double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
    setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()));
    setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
    fire(bulletPower);

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
