package com.googlecode.reunion.jreunion.game;

import java.util.Iterator;

import com.googlecode.reunion.jreunion.server.S_Client;
import com.googlecode.reunion.jreunion.server.S_Server;

/**
 * @author Aidamina
 * @license http://reunion.googlecode.com/svn/trunk/license.txt
 */
public class G_BulkanPlayer extends G_Player {
	
	public G_BulkanPlayer() {
		super();
	}
	
	public void meleeAttack(G_LivingObject livingObject){
		if(livingObject instanceof G_Mob)
			this.meleeAttackMob((G_Mob)livingObject);
		else
			if(livingObject instanceof G_Player)
				this.meleeAttackPlayer((G_Player)livingObject);
	}
	
	private void meleeAttackMob(G_Mob mob) {
		
		int baseDmg = this.getBaseDmg();
		//S_Server.getInstance().getWorldModule().getWorldCommand().serverSay("BaseDmg:"+baseDmg);
		int newHp = mob.getCurrHp() - baseDmg;
		
		if(newHp <= 0){
			
			mob.setDead();
			
			this.updateStatus(12,this.getLvlUpExp() - mob.getExp(),0);
			this.updateStatus(11,mob.getExp(),0);
			this.updateStatus(10,mob.getLime(),0);
			
			if(mob.getType() == 324){
				G_Item item = com.googlecode.reunion.jreunion.server.S_ItemFactory.createItem(1054); 
				
				item.setExtraStats((int)(Math.random()*10000));
				
				this.pickupItem(item.getEntityId());
				this.getQuest().questEnd(this,669);
				this.getQuest().questEff(this);
			}
		}
		else
			mob.setCurrHp(newHp);
	}
	
	private void meleeAttackPlayer(G_Player player) {
	
	}
	
	public int getBaseDmg() {
		int randDmg, baseDmg=0;
				
		randDmg = this.getMinDmg() + (int)(Math.random()*(this.getMaxDmg()-this.getMinDmg()));
				
		baseDmg = (int)(randDmg + this.getLevel()/6 + this.getStr()/4 + this.getDex()/4 + this.getCons()/8);
		
		if(this.getEquipment().getFirstHand() instanceof G_Sword)
			baseDmg = (int)(baseDmg + baseDmg*(float)(this.getCharSkill().getSkill(1).getCurrFirstRange()/100));
		else if(this.getEquipment().getFirstHand() instanceof G_Axe)
			baseDmg = (int)(baseDmg + baseDmg*(float)(this.getCharSkill().getSkill(2).getCurrFirstRange()/100));;
		
		return baseDmg;
	}
	
	public void useSkill(G_LivingObject livingObject, int skillId){
		
		G_Skill skill = this.getCharSkill().getSkill(skillId);
		
		if(skill.getType() == 0)
			permanentSkill(skill);
		else if(skill.getType() == 1)
			activationSkill(skill);
		else if(skill.getType() == 2)
			attackSkill(livingObject, skill);
	}
	
	public void activationSkill(G_Skill skill){
		
	}

	public void attackSkill(G_LivingObject livingObject, G_Skill skill){
		if(livingObject instanceof G_Mob)
			skillAttackMob((G_Mob)livingObject, skill);
		else if(livingObject instanceof G_Player)
			skillAttackPlayer((G_Player)livingObject, skill);
	}
	
	public void permanentSkill(G_Skill skill){
	
	}
	
	public void skillAttackMob(G_Mob mob, G_Skill skill){
		S_Client client = S_Server.getInstance().getNetworkModule().getClient(this);
		
		if(client == null)
			return;
				
		
		float baseDmg = (float)this.getBaseDmg();
		float skillDmg = baseDmg;
				
		if(skill.getId() == 31){ //Whirlwind Slash Skill
			if(this.getEquipment().getFirstHand() instanceof G_Sword)
				skillDmg = baseDmg + (baseDmg * ((float)(skill.getCurrFirstRange()/100)));
		}
		else if(skill.getId() == 18){ //Overhead Blow Skill
			if(this.getEquipment().getFirstHand() instanceof G_Axe)
				skillDmg = baseDmg + (baseDmg * ((float)(skill.getCurrFirstRange()/100)));
		}
				
		//S_Server.getInstance().getWorldModule().getWorldCommand().serverSay("SkillDmg:"+skillDmg);
		int newHp = mob.getCurrHp() - (int)skillDmg;
		
		this.updateStatus(skill.getStatusUsed(),this.getCurrStm() - (int)skill.getCurrConsumn(),this.getMaxStm());
				
		if(newHp <= 0){
			
			mob.setDead();
			
			this.updateStatus(12,this.getLvlUpExp()-mob.getExp(),0);
			this.updateStatus(11,mob.getExp(),0);
			this.updateStatus(10,mob.getLime(),0);
		}
		else
			mob.setCurrHp(newHp);
		
		int percentageHp = (mob.getCurrHp()*100)/mob.getMaxHp();
		
		if(percentageHp == 0 && mob.getCurrHp() > 0)
			percentageHp = 1;
					
		String packetData = "attack_vital npc " + mob.getEntityId()+
			" " + percentageHp + " 0 0\n";
		
		//S> attack_vital npc [NpcID] [RemainHP%] 0 0
		S_Server.getInstance().getNetworkModule().SendPacket(client.networkId, packetData);
		
		if (this.getSession().getPlayerListSize() > 0){
			Iterator<G_Player> playerIter = this.getSession().getPlayerListIterator();
		
			while(playerIter.hasNext()){
				G_Player pl = playerIter.next();
				
				client = S_Server.getInstance().getNetworkModule().getClient(pl);
				
				if(client == null)
					continue;
				
				packetData = "effect "+skill.getId()+" char "+this.getEntityId()+" npc "+mob.getEntityId()+
					" "+percentageHp+" 0 0\n";
								
				// S> effect [SkillID] char [charID] npc [npcID] [RemainNpcHP%] 0 0
				S_Server.getInstance().getNetworkModule().SendPacket(client.networkId, packetData);				
			}
		}
	}
	
	public void skillAttackPlayer(G_Player player, G_Skill skill){
		
	}
	
	public void levelUpSkill(G_Skill skill){
		S_Client client = S_Server.getInstance().getNetworkModule().getClient(this);
		
		if(client==null)
			return;
		
		String packetData = new String();
		
		getCharSkill().incSkill(this,skill); 
		packetData = "skilllevel "+ skill.getId() +" "+ skill.getCurrLevel()+"\n";
		S_Server.getInstance().getNetworkModule().SendPacket(client.networkId, packetData);
	}
}