package com.googlecode.reunion.jreunion.game;

import com.googlecode.reunion.jreunion.server.Client;


/**
 * @author Aidamina
 * @license http://reunion.googlecode.com/svn/trunk/license.txt
 */
public class AidiaPlayer extends Player {

	public AidiaPlayer(Client client) {
		super(client);
	}

	public int getBaseDmg(Player player) {
		int baseDmg, randDmg;

		randDmg = player.getMinDmg()
				+ (int) (Math.random() * (player.getMaxDmg() - player
						.getMinDmg()));

		baseDmg = (randDmg + getLevel() / 5 + getWis() / 3 + getLeadership());

		return baseDmg;
	}

	@Override
	public void meleeAttack(LivingObject livingObject) {
		if (livingObject instanceof Mob) {
			meleeAttackMob((Mob) livingObject);
		} else if (livingObject instanceof Player) {
			meleeAttackPlayer((Player) livingObject);
		}
	}

	private void meleeAttackMob(Mob mob) {
		int newHp;

		newHp = mob.getCurrHp() - getBaseDmg(this);

		if (getEquipment().getMainHand() != null) {
			getEquipment().getMainHand().consumn(this);
		}

		if (newHp <= 0) {

			mob.setDead(this);

			updateStatus(12, getLvlUpExp() - mob.getExp(), 0);
			updateStatus(11, mob.getExp(), 0);
			updateStatus(10, mob.getLime(), 0);

			if (mob.getType() == 324) {
				Item item = com.googlecode.reunion.jreunion.server.ItemFactory
						.createItem(1054);

				item.setExtraStats((int) (Math.random() * 10000));

				pickupItem(item.getEntityId());
				getQuest().questEnd(this, 669);
				getQuest().questEff(this);
			}
		} else {
			mob.setCurrHp(newHp);
		}
	}

	private void meleeAttackPlayer(Player player) {

	}

	@Override
	public void useSkill(LivingObject livingObject, int skillId) {

	}
}