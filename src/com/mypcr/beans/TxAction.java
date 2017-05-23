package com.mypcr.beans;


public class TxAction
{	
	private byte[] Tx_Buffer;
	
	public static final int	TX_BUFSIZE			= 65;
	
	public static final int	TX_CMD				= 1;
	public static final int	TX_ACTNO			= 2;
	public static final int	TX_TEMP				= 3;
	public static final int	TX_TIMEH			= 4;
	public static final int	TX_TIMEL			= 5;
	public static final int	TX_LIDTEMP			= 6;
	public static final int	TX_REQLINE			= 7;
	public static final int	TX_CURRENT_ACT_NO	= 8;
	public static final int	TX_BOOTLOADER		= 10;
	
	public static final int	TX_TIME_1			= 51;		// time for 4byte
	public static final int	TX_TIME_2			= 52;
	public static final int	TX_TIME_3			= 53;
	public static final int	TX_TIME_4			= 54;
	
	public static final int	AF_GOTO				= 250;
	
	public TxAction()
	{
		Tx_Buffer = new byte[TX_BUFSIZE];
	}
	
	private void Tx_Clear()
	{
		Tx_Buffer = new byte[TX_BUFSIZE];
	}
	
	public byte[] Tx_NOP()
	{
		Tx_Clear();
		return Tx_Buffer;
	}
	
	public byte[] Tx_TaskWrite(String label, String temp, String time, String preheat, int currentActNo)
	{
		Tx_Clear();
		int nlabel, ntemp, ntime, npreheat;
		if( label.equals("GOTO"))
			nlabel = AF_GOTO;
		else
			nlabel = Integer.parseInt(label);
		ntemp = Integer.parseInt(temp);
		ntime = Integer.parseInt(time);
		npreheat = Integer.parseInt(preheat);
		Tx_Buffer[TX_CMD] 		= Command.TASK_WRITE;
		Tx_Buffer[TX_ACTNO] 	= (byte)nlabel;
		Tx_Buffer[TX_TEMP] 		= (byte)ntemp; 
//		Tx_Buffer[TX_TIMEH] 	= (byte)(ntime/256.0);
//		Tx_Buffer[TX_TIMEL] 	= (byte)ntime;
//		Tx_Buffer[TX_TIMEH] 	= (byte)-1;
//		Tx_Buffer[TX_TIMEL] 	= (byte)-1;
		
//		Tx_Buffer[TX_TIME_1]	= (byte)((ntime>>24) & 0xff);		// Time for 4byte
//		Tx_Buffer[TX_TIME_2]	= (byte)((ntime>>16) & 0xff);
//		Tx_Buffer[TX_TIME_3]	= (byte)((ntime>>8) & 0xff);
//		Tx_Buffer[TX_TIME_4]	= (byte)(ntime);
		
		Tx_Buffer[TX_TIME_1]	= (byte)(ntime/16777216);	// Time for 4byte
		Tx_Buffer[TX_TIME_2]	= (byte)(ntime/65536);
		Tx_Buffer[TX_TIME_3]	= (byte)(ntime/256);
		Tx_Buffer[TX_TIME_4]	= (byte)(ntime);
		
		Tx_Buffer[TX_LIDTEMP] 	= (byte)npreheat;
		Tx_Buffer[TX_CURRENT_ACT_NO] = (byte)currentActNo;
		Tx_Buffer[TX_REQLINE]	= (byte)currentActNo;
		
		get_Info( Tx_Buffer );
		return Tx_Buffer;
	}
	
	public byte[] Tx_TaskEnd()
	{
		Tx_Clear();
		Tx_Buffer[TX_CMD] = Command.TASK_END;
		return Tx_Buffer;
	}
	
	public byte[] Tx_Go()
	{
		Tx_Clear();
		Tx_Buffer[TX_CMD] = Command.GO;
		return Tx_Buffer;
	}
	
	public byte[] Tx_Stop()
	{
		Tx_Clear();
		Tx_Buffer[TX_CMD] = Command.STOP;
		return Tx_Buffer;
	}
	
	public byte[] Tx_BootLoader()
	{
		Tx_Clear();
		Tx_Buffer[TX_BOOTLOADER] = Command.BOOTLOADER;
		return Tx_Buffer;
	}
	
	public byte[] Tx_RequestLine(byte reqline)
	{
		Tx_Clear();
		Tx_Buffer[TX_REQLINE] = reqline;
		return Tx_Buffer;
	}
	
	public void get_Info(byte[] buffer)
	{
		
		System.out.println( "--------------- TX --------------" );
		System.out.printf("%s\t\t: %d\n", "TX_CMD", buffer[TX_CMD]);
		System.out.printf("%s\t: %d\n", "TX_ACTNO", buffer[TX_ACTNO]);
		System.out.printf("%s\t\t: %d\n", "TX_TEMP", buffer[TX_TEMP]);
		System.out.printf("%s\t: %d\n", "TX_TIMEH", buffer[TX_TIMEH]);
		System.out.printf("%s\t: %d\n", "TX_TIMEL", buffer[TX_TIMEL]);
		System.out.printf("%s\t: %d\n", "TX_LIDTEMP", buffer[TX_LIDTEMP]);
		System.out.printf("%s\t: %d\n", "TX_REQLINE", buffer[TX_REQLINE]);
		System.out.printf("%s\t: %d\n", "TX_CURRENT_ACT_NO", buffer[TX_CURRENT_ACT_NO]);
		System.out.printf("%s\t: %d\n", "TX_BOOTLOADER", buffer[TX_BOOTLOADER]);
		System.out.printf("%s\t: %d\n", "TX_TIME_1", buffer[TX_TIME_1]);
		System.out.printf("%s\t: %d\n", "TX_TIME_2", buffer[TX_TIME_2]);
		System.out.printf("%s\t: %d\n", "TX_TIME_3", buffer[TX_TIME_3]);
		System.out.printf("%s\t: %d\n", "TX_TIME_4", buffer[TX_TIME_4]);
		System.out.println( "---------------------------------" );
	}
}
