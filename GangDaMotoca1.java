package GDM;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Random;

public class GangueDaMotocax1 extends AdvancedRobot {
  int moveDirection = 1;
  double enemyEnergy = 100;
  Random random = new Random();
  HashMap<String, EnemyBot> enemies = new HashMap<String, EnemyBot>();

  public void run() {
    
    setBodyColor(new Color(255, 105, 180));
    setRadarColor(new Color(255, 105, 180));
    setGunColor(new Color(255, 105, 180));
    setBulletColor(new Color(255, 105, 180));

    
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);

    
    while (true) {
      turnRadarRightRadians(Double.MAX_VALUE); 
      execute();
    }
  }

  public void onScannedRobot(ScannedRobotEvent e) {
    
    EnemyBot enemy = enemies.get(e.getName());
    if (enemy == null) {
      enemy = new EnemyBot();
      enemies.put(e.getName(), enemy);
    }
    enemy.update(e);

    
    EnemyBot target = chooseTarget();

    
    double absBearing = e.getBearingRadians() + getHeadingRadians();
    setTurnRightRadians(Utils.normalRelativeAngle(absBearing - getHeadingRadians() + Math.PI / 2 - moveDirection * Math.PI / 4));

    
    double distance = e.getDistance();  
    double bulletPower;
	
    if (distance > 500) {	
        bulletPower = 0.5;
    } else if (distance > 250) {
        bulletPower = 1.5;
    } else {
        bulletPower = 3.0;
    }

    double myX = getX();
    double myY = getY();
    double enemyX = getX() + distance * Math.sin(absBearing);
    double enemyY = getY() + distance * Math.cos(absBearing);
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

    double energyDrop = enemyEnergy - e.getEnergy();
    if (energyDrop > 0 && energyDrop <= 3) {
       
      moveDirection = -moveDirection;
      setAhead(50 * moveDirection);
    }
    enemyEnergy = e.getEnergy();

    double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
    double extraTurn = Math.min(Math.atan(36.0 / distance), Rules.RADAR_TURN_RATE_RADIANS);
    if (radarTurn < 0)
      radarTurn -= extraTurn;
    else
      radarTurn += extraTurn;
    setTurnRadarRightRadians(radarTurn);

    if (distance > 150) {
        setAhead((distance / 4 + 25) * moveDirection);
    } else {
        setBack((distance / 4 + 25) * moveDirection);
    }

    execute();
  }

  private EnemyBot chooseTarget() {
    EnemyBot target = null;
    for (EnemyBot enemy : enemies.values()) {
      if (target == null || enemy.getEnergy() < target.getEnergy()) {
        target = enemy;
      }
    }
    return target;
  }

  public void onHitWall(HitWallEvent e) {

   moveDirection = -moveDirection;

  setTurnRight(90 - e.getBearing()); 

  setAhead(100 * moveDirection); 

  execute();
  }

  public void onHitRobot(HitRobotEvent e) {
    moveDirection = -moveDirection;

     setTurnRight(90 - e.getBearing());

    execute();
  }
}

class EnemyBot {
  private double distance;
  private double energy;

  public void update(ScannedRobotEvent e) {
    distance = e.getDistance();
    energy = e.getEnergy();
  }

  public double getDistance() {
    return distance;
  }

  public double getEnergy() {
    return energy;
  }
}
