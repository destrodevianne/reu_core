/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.skills.funcs;

import java.lang.reflect.Constructor;

import l2r.gameserver.enums.StatFunction;
import l2r.gameserver.model.conditions.Condition;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkizub
 */
public final class FuncTemplate
{
	protected static final Logger _log = LoggerFactory.getLogger(FuncTemplate.class);
	
	public Condition attachCond;
	public Condition applayCond;
	public final Class<?> func;
	public final Constructor<?> constructor;
	private final Stats _stat;
	private final int _order;
	private final Lambda _lambda;
	
	public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int order, Lambda pLambda)
	{
		final StatFunction function = StatFunction.valueOf(pFunc.toUpperCase());
		if (order >= 0)
		{
			_order = order;
		}
		else
		{
			_order = function.getOrder();
		}
		
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		_stat = pStat;
		_lambda = pLambda;
		try
		{
			func = Class.forName("l2r.gameserver.model.skills.funcs.Func" + pFunc);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			constructor = func.getConstructor(new Class<?>[]
			{
				// Stats to update
				Stats.class,
				// Order of execution
				Integer.TYPE,
				// Owner
				Object.class,
				// Value for function
				Lambda.class
			});
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the function stat.
	 * @return the stat.
	 */
	public Stats getStat()
	{
		return _stat;
	}
	
	/**
	 * Gets the function priority order.
	 * @return the order
	 */
	public int getOrder()
	{
		return _order;
	}
	
	/**
	 * Gets the function lambda.
	 * @return the lambda
	 */
	public Lambda getLambda()
	{
		return _lambda;
	}
	
	public Func getFunc(Env env, Object owner)
	{
		if ((attachCond != null) && !attachCond.test(env))
		{
			return null;
		}
		try
		{
			Func f = (Func) constructor.newInstance(_stat, _order, owner, _lambda);
			if (applayCond != null)
			{
				f.setCondition(applayCond);
			}
			return f;
		}
		catch (Exception e)
		{
			_log.warn(String.valueOf(e));
			return null;
		}
	}
}
