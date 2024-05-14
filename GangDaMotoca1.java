package GDM;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Random;

public class GangueDaMotoca extends AdvancedRobot {
  int moveDirection = 1; // DireÃ§Ã£o do movimento do robÃ´
  double enemyEnergy = 100; // Energia do inimigo
  Random random = new Random(); // Objeto para gerar nÃºmeros aleatÃ³rios
  HashMap<String, EnemyBot> enemies = new HashMap<String, EnemyBot>(); // Mapa para armazenar os inimigos

  // MÃ©todo principal do robÃ´
  public void run() {
    // ConfiguraÃ§Ã£o das cores do robÃ´
    setBodyColor(new Color(255, 105, 180));
    setRadarColor(new Color(255, 105, 180));
    setGunColor(new Color(255, 105, 180));
    setBulletColor(new Color(255, 105, 180));

    // ConfiguraÃ§Ã£o do ajuste do canhÃ£o e do radar
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);

    // Loop principal do robÃ´
    while (true) {
      turnRadarRightRadians(Double.MAX_VALUE); // Gira o radar para a direita
      execute(); // Executa todas as aÃ§Ãµes pendentes
    }
  }

  // MÃ©todo chamado quando um robÃ´ Ã© escaneado
  public void onScannedRobot(ScannedRobotEvent e) {
    // Atualiza as informaÃ§Ãµes do inimigo
    EnemyBot enemy = enemies.get(e.getName());
    if (enemy == null) {
      enemy = new EnemyBot();
      enemies.put(e.getName(), enemy);
    }
    enemy.update(e, this);

    // Escolhe o alvo com base na distÃ¢ncia
    EnemyBot target = chooseTarget();

    // Calcula a direÃ§Ã£o absoluta do inimigo
    double absBearing = e.getBearingRadians() + getHeadingRadians();
    // Ajusta a direÃ§Ã£o do robÃ´ em relaÃ§Ã£o ao inimigo
    setTurnRightRadians(Utils.normalRelativeAngle(absBearing - getHeadingRadians() + Math.PI / 2 - moveDirection * Math.PI / 4));

    // Calcula a potÃªncia do tiro com base na distÃ¢ncia do inimigo
    double distance = e.getDistance();
    double bulletPower;
    if (distance > 500) {
        bulletPower = 0.5;
    } else if (distance > 250) {
        bulletPower = 1.5;
    } else {
        bulletPower = 3.0;
    }

    // Atira se a energia do robÃ´ for maior que 3
    if (getEnergy() > 3) {
      // Calcula a chance de acertar o tiro
      double hitChance = 1 - (distance / getBattleFieldWidth());
      // Atira se a chance de acertar for maior que um nÃºmero aleatÃ³rio
      if (random.nextDouble() < hitChance) {
        // Calcula a velocidade da bala
        double bulletSpeed = 20 - bulletPower * 3;
        // Calcula o tempo que a bala levarÃ¡ para atingir o inimigo
        long time = (long)(distance / bulletSpeed);
        
        // Calcula a posiÃ§Ã£o futura do inimigo
        double futureX = enemy.getX() + Math.sin(enemy.getHeadingRadians()) * enemy.getVelocity() * time;
        double futureY = enemy.getY() + Math.cos(enemy.getHeadingRadians()) * enemy.getVelocity() * time;
        // Calcula a direÃ§Ã£o futura do inimigo
        double futureBearing = Utils.normalAbsoluteAngle(Math.atan2(futureX - getX(), futureY - getY()));
        
        // Ajusta a direÃ§Ã£o do canhÃ£o e atira
        setTurnGunRightRadians(Utils.normalRelativeAngle(futureBearing - getGunHeadingRadians()));
        setFire(bulletPower);
      }
    }

    // Verifica se o inimigo atirou e ajusta a direÃ§Ã£o do movimento
    double energyDrop = enemyEnergy - e.getEnergy();
    if (energyDrop > 0 && energyDrop <= 3) {
      moveDirection = -moveDirection;
      setAhead(50 * moveDirection);
    }
    enemyEnergy = e.getEnergy();

    // Ajusta a direÃ§Ã£o do radar
    double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
    double extraTurn = Math.min(Math.atan(36.0 / distance), Rules.RADAR_TURN_RATE_RADIANS);
    if (radarTurn < 0)
      radarTurn -= extraTurn;
    else
      radarTurn += extraTurn;
    setTurnRadarRightRadians(radarTurn);

    // Ajusta a direÃ§Ã£o do movimento com base na distÃ¢ncia do inimigo
    if (distance > 150) {
        setAhead((distance / 4 + 25) * moveDirection);
    } else {
        setBack((distance / 4 + 25) * moveDirection);
    }

    execute(); // Executa todas as aÃ§Ãµes pendentes
  }

  // MÃ©todo para escolher o alvo com base na distÃ¢ncia
  private EnemyBot chooseTarget() {
    EnemyBot target = null;
    for (EnemyBot enemy : enemies.values()) {
      if (target == null || enemy.getDistance() < target.getDistance()) {
        target = enemy;
      }
    }
    return target;
  }

  // MÃ©todo chamado quando o robÃ´ atinge a parede
  public void onHitWall(HitWallEvent e) {
    moveDirection = -moveDirection; // Inverte a direÃ§Ã£o do movimento
    setTurnRight(90 - e.getBearing()); // Ajusta a direÃ§Ã£o do robÃ´
    setAhead(100 * moveDirection); // Move o robÃ´ para frente
    execute(); // Executa todas as aÃ§Ãµes pendentes
  }
}

// Classe para armazenar as informaÃ§Ãµes do inimigo
class EnemyBot {
  private double distance; // DistÃ¢ncia para o inimigo
  private double energy; // Energia do inimigo
  private double x; // PosiÃ§Ã£o x do inimigo
  private double y; // PosiÃ§Ã£o y do inimigo
  private double headingRadians; // DireÃ§Ã£o do inimigo em radianos
  private double velocity; // Velocidade do inimigo

  // MÃ©todo para atualizar as informaÃ§Ãµes do inimigo
  public void update(ScannedRobotEvent e, AdvancedRobot robot) {
    distance = e.getDistance();
    energy = e.getEnergy();
    double absBearing = e.getBearingRadians() + robot.getHeadingRadians();
    x = robot.getX() + e.getDistance() * Math.sin(absBearing);
    y = robot.getY() + e.getDistance() * Math.cos(absBearing);
    headingRadians = e.getHeadingRadians();
    velocity = e.getVelocity();
  }

  // MÃ©todos getters para obter as informaÃ§Ãµes do inimigo
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
}
 	 	
