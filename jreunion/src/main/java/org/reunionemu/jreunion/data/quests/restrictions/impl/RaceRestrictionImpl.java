package org.reunionemu.jreunion.data.quests.restrictions.impl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.reunionemu.jreunion.data.quests.restrictions.RaceRestriction;
import org.reunionemu.jreunion.game.Player.Race;

@XmlType(name="race")
public class RaceRestrictionImpl extends RestrictionImpl implements RaceRestriction {
	
	@XmlValue()
	protected Integer id;

	@Override
	public Integer getId() {
		return id;
	}


}