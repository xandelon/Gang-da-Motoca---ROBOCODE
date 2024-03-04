package NOSSOROBO;
import robocode.*;

public class GangDaMotoca1 extends AdvancedRobot
{

	public void run() {
		ahead(600);
		while(true) {
		//gira a arma 360 graus em loop
			turnGunLeft(360);	
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
	//atira e anda pra frente ( para nao sair da parede )
		fire(1);
		ahead(60);
	}

	public void onHitByBullet(HitByBulletEvent e) {
	//anda pra frente ao tomar um tiro
		ahead (60);
	}

	public void onHitWall(HitWallEvent e) {
		//faz o robo girar exatamente na parede
		turnLeft(90 - e.getBearing());
		ahead(30);
	}	
}