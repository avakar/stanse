/*
 * Copyright (c) 2009 Jiri Slaby <jirislaby@gmail.com>
 *
 * Distributed under GPLv2
 */

package cz.muni.stanse.codestructures;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.dom4j.Document;

/**
 * LRU implementation of UnitManager
 */
public final class UnitManagerLRU implements UnitManager {
    private LinkedList<Unit> LRU = new LinkedList<Unit>();
    private int water = 10;
    private int elements = 0;
    private static Logger logger = Logger.getLogger(
	    UnitManager.class.getName());

    public Collection<CFGHandle> getCFGHandles(Unit unit) {
	return touchUnit(unit).getCFGHandles();
    }

    public Document getXMLDocument(Unit unit) {
	return touchUnit(unit).getXMLDocument();
    }

    public String getUnitName(CFGHandle cfg) {
	return cfg.getUnit().getName();
    }

    public Unit touchUnit(Unit unit) {
	if (!LRU.remove(unit))
	    if (elements > water) {
		Unit toDrop = LRU.removeLast();
		logger.info("Dropping " + toDrop.getName());
		toDrop.drop();
	    } else
		elements++;
	LRU.addFirst(unit);
	return unit;
    }
}
