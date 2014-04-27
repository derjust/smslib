
package org.smslib.message;

import org.smslib.helper.Common;

/**
 * Represents a number usually associated with a message. 
 * An instance has always a not-null address and type 
 */
public class MsIsdn
{
	public enum Type
	{
		National, International, Text, Void
	}

	final String address;

	final Type type;

	public MsIsdn()
	{
		this("", Type.Void);
	}

	public MsIsdn(String number)
	{
		if (number.length() > 0 && number.charAt(0) == '+')
		{
			this.address = number.substring(1);
			this.type = Type.International;
		}
		else
		{
			this.address = number;
			this.type = typeOf(number);
		}
	}

	public MsIsdn(String address, Type type)
	{
		this.address = address;
		this.type = type;
	}

	public MsIsdn(MsIsdn msisdn)
	{
		this.type = msisdn.getType();
		this.address = msisdn.getAddress();
	}

	/**
	 * @return The addresss of this ISDN
	 */
	public String getAddress()
	{
		return this.address;
	}

	/**
	 * @return The type of this ISDN
	 */
	public Type getType()
	{
		return this.type;
	}

	/**
	 * @return True if {@link #getType()} equals {@link Type#Void}. Otherwise false
	 */
	public boolean isVoid()
	{
		return (this.type == Type.Void);
	}

	/**
	 * Two instances of MsIsdn are equals if they are ignore-case equal address
	 * @see MsIsdn#getAddress()
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof MsIsdn)) return false;
		return (this.address.equalsIgnoreCase(((MsIsdn) o).getAddress()));
	}

	@Override
	public String toString()
	{
		return String.format("[%s / %s]", getType(), getAddress());
	}

	@Override
	public int hashCode()
	{
		return this.address.hashCode() + (15 * this.type.hashCode());
	}

	private static Type typeOf(String number)
	{
		if (Common.isNullOrEmpty(number)) return Type.Void;
		for (int i = 0; i < number.length(); i++)
		{
			if (!Character.isDigit(number.charAt(i))) return Type.Text;
		}
		return Type.International;
	}
}
