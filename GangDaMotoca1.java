package GDM;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.HashMap;

public class GangueDaMotoca extends AdvancedRobot {
  int moveDirection = 1; // DireÃ§Ã£o do movimento do robÃ´
  double enemyEnergy = 100; // Energia do inimigo
  Random random = new Random(); // Gerador de nÃºmeros aleatÃ³rios
  HashMap<String, EnemyBot> enemies = new HashMap<String, EnemyBot>(); // Mapa para armazenar os inimigos detectados

  // MÃ©todo principal do robÃ´
  public void run() {
    // ConfiguraÃ§Ã£o das cores do robÃ´
    setBodyColor(new Color(255, 105, 180));
    setRadarColor(new Color(255, 105, 180));
    setGunColor(new Color(255, 105, 180));
    setBulletColor(new Color(255, 105, 180));

    // ConfiguraÃ§Ã£o para o canhÃ£o e o radar se moverem independentemente do robÃ´
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);

    // Loop principal do robÃ´
    while (true) {
      // Gira o radar para a direita continuamente
      turnRadarRightRadians(Double.MAX_VALUE);
      // Executa as aÃ§Ãµes do robÃ´
      execute();
    }
  }

  // MÃ©todo chamado quando um robÃ´ inimigo Ã© detectado
  public void onScannedRobot(ScannedRobotEvent e) {
    // ObtÃ©m o inimigo do mapa de inimigos
    EnemyBot enemy = enemies.get(e.getName());
    // Se o inimigo nÃ£o existir no mapa, cria um novo
    if (enemy == null) {
      enemy = new EnemyBot();
      enemies.put(e.getName(), enemy);
    }
    // Atualiza as informaÃ§Ãµes do inimigo
    enemy.update(e, this);

    // Escolhe o alvo com base na energia
    EnemyBot target = chooseTarget();

    // Calcula o Ã¢ngulo absoluto do inimigo
    double absBearing = e.getBearingRadians() + getHeadingRadians();
    // Define a direÃ§Ã£o do robÃ´
    setTurnRightRadians(Utils.normalRelativeAngle(absBearing - getHeadingRadians() + Math.PI / 2 - moveDirection * Math.PI / 4));

    // Calcula a distÃ¢ncia para o inimigo
    double distance = e.getDistance();
    // Define a potÃªncia do tiro com base na distÃ¢ncia
    double bulletPower;
  
    if (distance > 500) {
        bulletPower = 0.5;
    } else if (distance > 250) {
        bulletPower = 1.5;
    } else {
        bulletPower = 3.0;
    }

    // Se a energia do robÃ´ for maior que 3
    if (getEnergy() > 3) {
      // Calcula a chance de acertar o tiro
      double hitChance = 1 - (distance / getBattleFieldWidth());
      // Se a chance de acertar for maior que um nÃºmero aleatÃ³rio
      if (random.nextDouble() < hitChance) {
        // Calcula a velocidade da bala
        double bulletSpeed = 20 - bulletPower * 3;
        // Calcula o tempo que a bala levarÃ¡ para atingir o inimigo
        long time = (long)(distance / bulletSpeed);
        
        // Calcula a futura posiÃ§Ã£o x do inimigo
        double futureX = enemy.getX() + Math.sin(enemy.getHeadingRadians()) * enemy.getVelocity() * time;
        // Calcula a futura posiÃ§Ã£o y do inimigo
        double futureY = enemy.getY() + Math.cos(enemy.getHeadingRadians()) * enemy.getVelocity() * time;
        // Calcula o Ã¢ngulo futuro do inimigo
        double futureBearing = Utils.normalAbsoluteAngle(Math.atan2(futureX - getX(), futureY - getY()));
        
        // Define a direÃ§Ã£o do canhÃ£o
        setTurnGunRightRadians(Utils.normalRelativeAngle(futureBearing - getGunHeadingRadians()));
        // Atira
        setFire(bulletPower);
      }
    }

    // Calcula a queda de energia do inimigo
    double energyDrop = enemyEnergy - e.getEnergy();
    // Se a energia do inimigo caiu, muda a direÃ§Ã£o do robÃ´
    if (energyDrop > 0 && energyDrop <= 3) {
      moveDirection = -moveDirection;
      setAhead(50 * moveDirection);
    }
    // Atualiza a energia do inimigo
    enemyEnergy = e.getEnergy();

    // Calcula a direÃ§Ã£o do radar
    double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
    // Calcula o giro extra do radar
    double extraTurn = Math.min(Math.atan(36.0 / distance), Rules.RADAR_TURN_RATE_RADIANS);
    // Se a direÃ§Ã£o do radar for menor que 0, diminui o giro extra
    if (radarTurn < 0)
      radarTurn -= extraTurn;
    else
      radarTurn += extraTurn;
    // Define a direÃ§Ã£o do radar
    setTurnRadarRightRadians(radarTurn);

    // Se a distÃ¢ncia para o inimigo for maior que 150, move o robÃ´ para frente
    if (distance > 150) {
        setAhead((distance / 4 + 25) * moveDirection);
    } else {
        setBack((distance / 4 + 25) * moveDirection);
    }

    // Executa as aÃ§Ãµes do robÃ´
    execute();
  }

  // MÃ©todo para escolher o alvo com base na energia
  private EnemyBot chooseTarget() {
    EnemyBot target = null;
    for (EnemyBot enemy : enemies.values()) {
      if (target == null || enemy.getEnergy() < target.getEnergy()) {
        target = enemy;
      }
    }
    return target;
  }

  // MÃ©todo chamado quando o robÃ´ atinge a parede
  public void onHitWall(HitWallEvent e) {
    // Muda a direÃ§Ã£o do robÃ´
    moveDirection = -moveDirection;
    // Define a direÃ§Ã£o do robÃ´
    setTurnRight(90 - e.getBearing());
    // Move o robÃ´ para frente
    setAhead(100 * moveDirection);
    // Executa as aÃ§Ãµes do robÃ´
    execute();
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
