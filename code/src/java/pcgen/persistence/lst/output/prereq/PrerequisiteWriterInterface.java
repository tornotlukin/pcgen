/*
 * PrerequisiteWriterInterface.java
 *
 * Copyright 2004 (C) Frugal <frugal@purplewombat.co.uk>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.       See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on 18-Dec-2003
 *
 * Current Ver: $Revision$
 *
 *
 *
 */
package pcgen.persistence.lst.output.prereq;

import pcgen.core.prereq.Prerequisite;
import pcgen.core.prereq.PrerequisiteOperator;
import pcgen.persistence.PersistenceLayerException;

import java.io.Writer;

public interface PrerequisiteWriterInterface
{

	/**
	 * @return String
	 */
	String kindHandled();

	/**
	 * @return PrerequisiteOperator[]
	 */
	PrerequisiteOperator[] operatorsHandled();

	/**
	 * @param writer
	 * @param prereq
	 * @throws PersistenceLayerException
	 */
	void write(Writer writer, Prerequisite prereq)
		throws PersistenceLayerException;

}
