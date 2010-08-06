package com.googlecode.reunion.jreunion.server;


import java.util.Iterator;
import java.util.Vector;

import com.googlecode.reunion.jreunion.game.*;

/**
 * @author Aidamina
 * @license http://reunion.googlecode.com/svn/trunk/license.txt
 */
public class S_MobManager {
	private java.util.List<G_Mob> mobList = new Vector<G_Mob>();
	
	private boolean moveToPlayer = true;

	public S_MobManager() {
		
	}

	public void addMob(G_Mob mob) {
		if(containsMob(mob))
			return;
		mobList.add(mob);
	}
	public void removeMob(G_Mob mob) {
		if(!containsMob(mob))
			return;
		while(containsMob(mob))
			mobList.remove(mob);
		G_EntityManager.getEntityManager().destroyEntity(mob);
	}
	public boolean containsMob(G_Mob mob) {
		return mobList.contains(mob);
	}

	public int getNumberOfMobs() {
		return mobList.size();
	}

	public G_Mob createMob(int type) {
		G_Mob mob = new G_Mob(type);
		G_EntityManager.getEntityManager().createEntity(mob);
		addMob(mob);

		return mob;
	}

	public G_Mob getMob(int uniqueid) {
		Iterator<G_Mob> iter = getMobListIterator();
		while (iter.hasNext()) {
			G_Mob mob = iter.next();
			if (mob.getEntityId() == uniqueid)
				return mob;
		}
		return null;
	}

	public int getMobDirectionX(G_Mob mob){
		
		double directionX = (Math.random() * 2);
		
		if(directionX >= 1.5)
			return (mob.getPosX() + (int)(directionX * mob.getSpeed()));
		else
			return (mob.getPosX() + (int)(-directionX * mob.getSpeed()));
	}
	
	public int getMobDirectionY(G_Mob mob){
		
		double directionY = (Math.random() * 2);
		
		if(directionY >= 1.5)
			return (mob.getPosY() + (int)(directionY * mob.getSpeed()));
		else
			return (mob.getPosY() + (int)(-directionY * mob.getSpeed()));
	}
	
	public Iterator<G_Mob> getMobListIterator() {
		return mobList.iterator();
	}
	
	public void workMob(G_Mob mob) {
		
		if(mobList.size()==0)
			return;
				
		int run = 0;
		//int newPosX,newPosY;
		//double directionX=0, directionY=0;
									
		if(moveToPlayer == false)
			moveToPlayer = true;
			
		if(mob.getRunning() == true)
			run = 1;
		else
			run = 0;
				
		// Members of the new position to where the mob should move
		int newPosX = getMobDirectionX(mob);
		int newPosY = getMobDirectionY(mob);
		
		// Members for the random direction of mob
		/*directionX = Math.random()*2;
		directionY = Math.random()*2;
			
		if(directionX >= 1.5)
			newPosX = mob.getPosX()+(int)(directionX*mob.getSpeed());
		else
			newPosX = mob.getPosX()+(int)(-directionX*mob.getSpeed());
			
		if(directionY >= 1.5)
			newPosY = mob.getPosY()+(int)(directionY*mob.getSpeed());
		else
			newPosY = mob.getPosY()+(int)(-directionY*mob.getSpeed());*/
		
		Iterator<G_Player> iterPlayer = S_Server.getInstance().getWorldModule().getPlayerManager().getPlayerListIterator();
						
		while(iterPlayer.hasNext()){
		  G_Player player = (G_Player)iterPlayer.next();
		  S_Client client = S_Server.getInstance().getNetworkModule().getClient(player);
				
		  if(client==null)
			  continue;
			  
		  else if(client.getState() != S_Enums.CS_INGAME || mob.getMap() != player.getMap())
			  	continue;
			  
		  int distance = mob.getDistance(player);
		  
		  /*double xcomp = Math.pow(player.getPosX() - mob.getPosX(), 2);
		  double ycomp = Math.pow(player.getPosY() - mob.getPosY(), 2);
		  double distance = Math.sqrt(xcomp + ycomp);*/
				
		  // Condition that verify if the mob can move freely or not.
		  // If the distance between the mob and the player is less or equal 
		  // then 150 (distance that makes the mob move to the player direction)
		  // and if the player position is a walkable position for mob then the
		  // mob will chase the player, else the mob will mob freely.
		  if((int)distance <= 150)
			if(S_Server.getInstance().getWorldModule().getMapManager().getMobArea().get(((player.getPosX()/10)-300),(player.getPosY()/10)) == true)
				moveToPlayer = false;
		  
		  // Condition that detects that the mob its out of player session range
		  if((int)distance >= S_Server.getInstance().getWorldModule().getSessionManager().getSessionRadius())
			  player.getSession().exitMob(mob);
		  
		  if(distance < S_Server.getInstance().getWorldModule().getSessionManager().getSessionRadius())
			  if(mob.getIsAttacking() == 0){
				  String packetData = "walk npc " + mob.getEntityId() + " "
					+ mob.getPosX() + " " + mob.getPosY() + " 0 " + run + "\n";
				  //S> walk npc [UniqueId] [Xpos] [Ypos] [ZPos] [Running]
		
				  S_Server.getInstance().getNetworkModule().SendPacket(client.networkId, packetData);
			  }
		}
					
		if(moveToPlayer == true){
			G_Spawn spawn = S_Server.getInstance().getWorldModule().getMapManager().getSpawnByMob(mob.getEntityId());
			double radiusCompX = Math.pow(spawn.getCenterX() - newPosX, 2);
			double radiusCompY = Math.pow(spawn.getCenterY() - newPosY, 2);
			double radiusComp = Math.sqrt(radiusCompX + radiusCompY);
							
			if((int)radiusComp <= spawn.getRadius())
			{
				//System.out.print("Distance <= Radius\n");
				mob.setPosX(newPosX);
				mob.setPosY(newPosY);
			}
		}
	}
}