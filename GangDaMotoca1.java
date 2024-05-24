package gdm;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Random;

public class GangueDaMotoca extends AdvancedRobot {
  int moveDirection = 1;
  double enemyEnergy = 100;
  Random random = new Random();
  HashMap<String, EnemyBot> enemies = new HashMap<String, EnemyBot>();
  static final double PI = Math.PI;

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
    double oldVelocity = enemy.getVelocity();
    enemy.update(e, this);
    double velocityChange = enemy.getVelocity() - oldVelocity;
    enemy.getMovementStats().update(velocityChange, e.getDistance());

    EnemyBot target = chooseTarget();

    double absBearing = e.getBearingRadians() + getHeadingRadians();
    setTurnRightRadians(Utils.normalRelativeAngle(absBearing - getHeadingRadians() + PI / 2 - moveDirection * PI / 4));

    double distance = e.getDistance();
    double bulletPower;
    if (distance > 500) {
        bulletPower = 0.5;
    } else if (distance > 250) {
        bulletPower = 1.5;
    } else {
        bulletPower = 3.0;
    }

    if (getEnergy() > 3) {
      double hitChance = 1 - (distance / getBattleFieldWidth());
      if (random.nextDouble() < hitChance) {
        double bulletSpeed = 20 - bulletPower * 3;
        long time = (long)(distance / bulletSpeed);
        
        int bestIndex = enemy.getMovementStats().getBestIndex(e.getDistance());
        double predictedVelocityChange = (bestIndex - 500) / 10.0;
        
        double futureX = enemy.getX() + Math.sin(enemy.getHeadingRadians()) * (enemy.getVelocity() + predictedVelocityChange) * time;
        double futureY = enemy.getY() + Math.cos(enemy.getHeadingRadians()) * (enemy.getVelocity() + predictedVelocityChange) * time;
        double futureBearing = Utils.normalAbsoluteAngle(Math.atan2(futureX - getX(), futureY - getY()));
        
        setTurnGunRightRadians(Utils.normalRelativeAngle(futureBearing - getGunHeadingRadians()));
        setFire(bulletPower);
      }
    }

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
      if (target == null || enemy.getDistance() < target.getDistance()) {
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
}

class EnemyBot {
  private double distance;
  private double energy;
  private double x;
  private double y;
  private double headingRadians;
  private double velocity;
  private EnemyMovementStats movementStats = new EnemyMovementStats();

  public void update(ScannedRobotEvent e, AdvancedRobot robot) {
    distance = e.getDistance();
    energy = e.getEnergy();
    double absBearing = e.getBearingRadians() + robot.getHeadingRadians();
    x = robot.getX() + e.getDistance() * Math.sin(absBearing);
    y = robot.getY() + e.getDistance() * Math.cos(absBearing);
    headingRadians = e.getHeadingRadians();
    velocity = e.getVelocity();
  }

  public double getDistance() {
    return distance;
  }

  public double getEnergy() {
    return energy;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getHeadingRadians() {
    return headingRadians;
  }

  public double getVelocity() {
    return velocity;
  }

  public EnemyMovementStats getMovementStats() {
    return movementStats;
  }
}

class EnemyMovementStats {
  private int[][] stats = new int[5][1000];

  public void update(double velocityChange, double distance) {
    int distanceIndex = (int) (distance / 200);
    if (distanceIndex > 4) {
      distanceIndex = 4;
    }
    int index = (int) Math.round(velocityChange * 10) + 500;
    stats[distanceIndex][index]++;
  }

  public int getBestIndex(double distance) {
    int distanceIndex = (int) (distance / 200);
    if (distanceIndex > 4) {
      distanceIndex = 4;
    }
    int bestIndex = 500;
    for (int i = 0; i < stats[distanceIndex].length; i++) {
      if (stats[distanceIndex][i] > stats[distanceIndex][bestIndex]) {
        bestIndex = i;
      }
    }
    return bestIndex;
  }
}
