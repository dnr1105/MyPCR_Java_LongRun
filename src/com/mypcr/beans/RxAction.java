package com.mypcr.beans;

public class RxAction
{
	private int State;
	private int Cover_TempH;
	private int Cover_TempL;
	private int Chamber_TempH;
	private int Chamber_TempL;
	private int Heatsink_TempH;
	private int Heatsink_TempL;
	private int Current_Operation;
	private int Current_Action;
	private int Current_Loop;
	private int Total_Action;
	private int Error;
	private int Serial_H;
	private int Serial_L;
	private long Total_TimeLeft;
	private double Sec_TimeLeft;
	private int Firmware_Version;
	
	// adding for task write
	private int Label;
	private int Temp;
	private int Time_H;
	private int Time_L;
	private int ReqLine;

	private boolean IsReceiveOnce = false;
	
	// time for 4byte
	private long left_time_q;
	private long left_time_h;
	private long left_time_l;
	
	private int kp;
	private int ki;
	private int kd;
	
	private int time_1;
	private int time_2;
	private int time_3;
	private int time_4;
	private int total_time;

	public static final int	RX_BUFSIZE		= 64;
	
	public static final int	RX_STATE		= 0;
	public static final int	RX_RES			= 1;
	public static final int	RX_CURRENTACTNO	= 2;
	public static final int	RX_CURRENTLOOP	= 3;
	public static final int	RX_TOTALACTNO	= 4;
	public static final int	RX_KP			= 5;
	public static final int	RX_KI			= 6;
	public static final int	RX_KD			= 7;
	public static final int	RX_LEFTTIMEH	= 8;
	public static final int	RX_LEFTTIMEL	= 9;
	public static final int	RX_LEFTSECTIMEH	= 10;
	public static final int	RX_LEFTSECTIMEL	= 11;
	public static final int	RX_LIDTEMPH		= 12;
	public static final int	RX_LIDTEMPL		= 13;
	public static final int	RX_CHMTEMPH		= 14;
	public static final int	RX_CHMTEMPL		= 15;
	public static final int	RX_PWMH			= 16;
	public static final int	RX_PWML			= 17;
	public static final int	RX_PWMDIR		= 18;
	public static final int	RX_LABEL		= 19;
	public static final int	RX_TEMP			= 20;
	public static final int	RX_TIMEH		= 21;
	public static final int	RX_TIMEL		= 22;
	public static final int	RX_LIDTEMP		= 23;
	public static final int	RX_REQLINE		= 24;
	public static final int	RX_ERROR		= 25;
	public static final int	RX_CUR_OPR		= 26;
	public static final int	RX_SINKTEMPH	= 27;
	public static final int	RX_SINKTEMPL	= 28;
	public static final int	RX_KP_1			= 39;
	public static final int	RX_KI_1			= 33;
	public static final int	RX_KD_1			= 37;
	public static final int	RX_SERIALH		= 41;		// not using this version.
	public static final int	RX_SERIALL		= 42;		// only bluetooth version
	public static final int	RX_SERIALRESERV	= 43;
	public static final int	RX_VERSION		= 44;
	
	public static final int	RX_TIME_1		= 50;
	public static final int	RX_TIME_2		= 51;
	public static final int	RX_TIME_3		= 52;
	public static final int	RX_TIME_4		= 53;
	
	public static final int	RX_LEFTTIMEQ	= 54;
	public static final int	RX_LEFTSECTIMEQ	= 55;
	
	public static final int	AF_GOTO			= 250;

	public RxAction( )
	{
		State = 0;
		Cover_TempH = 0;
		Cover_TempL = 0;
		Chamber_TempH = 0;
		Chamber_TempL = 0;
		Heatsink_TempH = 0;
		Heatsink_TempL = 0;
		Current_Operation = 0;
		Current_Action = 0;
		Current_Loop = -1;
		Total_Action = 0;
		Error = 0;
		Total_TimeLeft = 0;
		Sec_TimeLeft = 0;
		Serial_H = 0;
		Serial_L = 0;
		Label = 0;
		Temp = 0;
		Time_H = 0;
		Time_L = 0;
		ReqLine = 0;
		
		left_time_q = 0;
		left_time_h = 0;
		left_time_l = 0;
		kp = 0;
		ki = 0;
		kd = 0;
		time_1 = 0;
		time_2 = 0;
		time_3 = 0;
		time_4 = 0;
		total_time = 0;
	}

	public void set_Info(byte[] buffer)
	{
		State 				= (int)(buffer[RX_STATE] & 0xff);
		Current_Action 		= (int)(buffer[RX_CURRENTACTNO] & 0xff);
		Current_Loop		= (int)(buffer[RX_CURRENTLOOP] & 0xff);
		Total_Action		= (int)(buffer[RX_TOTALACTNO] & 0xff);
		kp					= (int)(buffer[RX_KP] & 0xff);
		ki					= (int)(buffer[RX_KI] & 0xff);
		kd					= (int)(buffer[RX_KD] & 0xff);
		
		left_time_q			= (long)( buffer[RX_LEFTTIMEQ]);
		left_time_h			= (long)( buffer[RX_LEFTTIMEH]);
		left_time_l			= (long)( buffer[RX_LEFTTIMEL]);
		
		Total_TimeLeft		= (long)( (( buffer[RX_LEFTTIMEQ] & 0xff ) << 16)
                    				+ (( buffer[RX_LEFTTIMEH] & 0xff) << 8 )
                    				+ ( buffer[RX_LEFTTIMEL] & 0xff ) );
		
		Sec_TimeLeft		= (double) ( (( buffer[RX_LEFTSECTIMEQ] & 0xff ) << 16)
									+ ((buffer[RX_LEFTSECTIMEH] & 0xff ) << 8)
									+ ( buffer[RX_LEFTSECTIMEL] & 0xff ));
		
		Cover_TempH			= (int)(buffer[RX_LIDTEMPH] & 0xff);
		Cover_TempL			= (int)(buffer[RX_LIDTEMPL] & 0xff);
		Chamber_TempH		= (int)(buffer[RX_CHMTEMPH] & 0xff);
		Chamber_TempL		= (int)(buffer[RX_CHMTEMPL] & 0xff);
		Heatsink_TempH		= (int)(buffer[RX_SINKTEMPH] & 0xff);
		Heatsink_TempL		= (int)(buffer[RX_SINKTEMPL] & 0xff);
		Current_Operation	= (int)(buffer[RX_CUR_OPR] & 0xff);
		Error				= (int)(buffer[RX_ERROR] & 0xff);
		Serial_H			= (int)(buffer[RX_SERIALH] & 0xff);
		Serial_L			= (int)(buffer[RX_SERIALL] & 0xff);
		Firmware_Version	= (int)(buffer[RX_VERSION] & 0xff);
		Label 				= (int)(buffer[RX_LABEL] & 0xff);
		Temp				= (int)(buffer[RX_TEMP] & 0xff);
		Time_H				= (int)(buffer[RX_TIMEH] & 0xff);
		Time_L				= (int)(buffer[RX_TIMEL] & 0xff);
		ReqLine				= (int)(buffer[RX_REQLINE] & 0xff);
		
		time_1				= (int)(buffer[RX_TIME_1] & 0xff);
		time_2				= (int)(buffer[RX_TIME_2] & 0xff);
		time_3				= (int)(buffer[RX_TIME_3] & 0xff);
		time_4				= (int)(buffer[RX_TIME_4] & 0xff);
		total_time			= (int)(((buffer[RX_TIME_1] & 0xff) << 24)
							+ ((buffer[RX_TIME_2] & 0xff) << 16)
							+ ((buffer[RX_TIME_3] & 0xff) << 8)
							+ ((buffer[RX_TIME_4] & 0xff) ));
//		total_time			= (int)(((buffer[RX_TIME_1] & 0xff) * 16777216)
//                				+ ((int)(buffer[RX_TIME_2] & 0xff) * 65536)
//                				+ ((int)(buffer[RX_TIME_3] & 0xff) * 256)
//                				+ ((int)(buffer[RX_TIME_4] & 0xff) ));
		
		IsReceiveOnce = true;
	}
	
	public int ki()
	{
		return this.ki;
	}
	
	public long getLefttime_Q()
	{
		return this.left_time_q;
	}
	
	public long getLefttime_H()
	{
		return this.left_time_h;
	}
	
	public long getLefttime_L()
	{
		return this.left_time_l;
	}

	public boolean IsValidBuffer()
	{
		return IsReceiveOnce;
	}

	public void setTotal_Action(int Total_Action)
	{
		this.Total_Action = (byte)Total_Action;
	}

	public int getState()
	{
		return State;
	}

	public int getCover_TempH()
	{
		return Cover_TempH;
	}

	public int getCover_TempL()
	{
		return Cover_TempL;
	}

	public int getChamber_TempH()
	{
		return Chamber_TempH;
	}

	public int getChamber_TempL()
	{
		return Chamber_TempL;
	}

	public int getHeatsink_TempH()
	{
		return Heatsink_TempH;
	}

	public int getHeatsink_TempL()
	{
		return Heatsink_TempL;
	}

	public int getCurrent_Operation()
	{
		return Current_Operation;
	}

	public int getCurrent_Action()
	{
		return Current_Action;
	}

	public int getCurrent_Loop()
	{
		return Current_Loop;
	}

	public int getTotal_Action()
	{
		return Total_Action;
	}

	public int getError()
	{
		return Error;
	}

	public long getTotal_TimeLeft()
	{
		return Total_TimeLeft;
	}

	public double getSec_TimeLeft()
	{
		return Sec_TimeLeft;
	}

	public int getSerial_H()
	{
		return Serial_H;
	}

	public int getSerial_L()
	{
		return Serial_L;
	}

	public int getFirmware_Version()
	{
		return Firmware_Version;
	}
	
	public int getLabel()
	{
		return Label;
	}
	
	public int getTemp()
	{
		return Temp;
	}
	
	public int getTime_H()
	{
		return Time_H;
	}
	
	public int getTime_L()
	{
		return Time_L;
	}
	
	public int getReqLine()
	{
		return ReqLine;
	}
	
	public int getTime_1()
	{
		return this.time_1;
	}
	
	public int getTime_2()
	{
		return this.time_2;
	}
	
	public int getTime_3()
	{
		return this.time_3;
	}
	
	public int getTime_4()
	{
		return this.time_4;
	}
	
	public int getTotal_time()
	{
		return this.total_time;
	}
	
	public void get_Info(byte[] buffer)
	{
//		for(int i = 50; i < 54; i++)
//		{
//			System.out.println( i+"\t: "+buffer[i] );
//		}
		
		System.out.println( "--------------- RX --------------" );
		System.out.printf("%s\t: %d\n", "RX_STATE", (short)buffer[RX_STATE]);
		System.out.printf("%s\t\t: %d\n", "RX_RES", (int)buffer[RX_RES]);
		System.out.printf("%s\t: %d\n", "RX_CURRENTACTNO", (int)buffer[RX_CURRENTACTNO]);
		System.out.printf("%s\t: %d\n", "RX_CURRENTLOOP", (int)buffer[RX_CURRENTLOOP]);
		System.out.printf("%s\t: %d\n", "RX_TOTALACTNO", (int)buffer[RX_TOTALACTNO]);
		System.out.printf("%s\t\t: %d\n", "RX_KP", (int)buffer[RX_KP]);
		System.out.printf("%s\t\t: %d\n", "RX_KI", (int)(buffer[RX_KI]));
		System.out.printf("%s\t\t: %d\n", "RX_KD", (int)buffer[RX_KD]);
		System.out.printf("%s\t: %d\n", "RX_LEFTTIMEQ", (int)buffer[RX_LEFTTIMEQ]);
		System.out.printf("%s\t: %d\n", "RX_LEFTTIMEH", (int)buffer[RX_LEFTTIMEH]);
		System.out.printf("%s\t: %d\n", "RX_LEFTTIMEL", (int)buffer[RX_LEFTTIMEL]);
		System.out.printf("%s\t: %d\n", "RX_LEFTSECTIMEQ", (int)buffer[RX_LEFTSECTIMEQ]);
		System.out.printf("%s\t: %d\n", "RX_LEFTSECTIMEH", (int)buffer[RX_LEFTSECTIMEH]);
		System.out.printf("%s\t: %d\n", "RX_LEFTSECTIMEL", (int)buffer[RX_LEFTSECTIMEL]);
		System.out.printf("%s\t: %d\n", "RX_LIDTEMPH", (int)buffer[RX_LIDTEMPH]);
		System.out.printf("%s\t: %d\n", "RX_LIDTEMPL", (int)buffer[RX_LIDTEMPL]);
		System.out.printf("%s\t: %d\n", "RX_CHMTEMPH", (int)buffer[RX_CHMTEMPH]);
		System.out.printf("%s\t: %d\n", "RX_CHMTEMPL", (int)buffer[RX_CHMTEMPL]);
		System.out.printf("%s\t\t: %d\n", "RX_PWMH", (int)buffer[RX_PWMH]);
		System.out.printf("%s\t\t: %d\n", "RX_PWML", (int)buffer[RX_PWML]);
		System.out.printf("%s\t: %d\n", "RX_PWMDIR", (int)buffer[RX_PWMDIR]);
		System.out.printf("%s\t: %d\n", "RX_LABEL", (int)buffer[RX_LABEL]);
		System.out.printf("%s\t\t: %d\n", "RX_TEMP", (int)buffer[RX_TEMP]);
		System.out.printf("%s\t: %d\n", "RX_TIMEH", (int)buffer[RX_TIMEH]);
		System.out.printf("%s\t: %d\n", "RX_TIMEL", (int)buffer[RX_TIMEL]);
		System.out.printf("%s\t: %d\n", "RX_LIDTEMP", (int)buffer[RX_LIDTEMP]);
		System.out.printf("%s\t: %d\n", "RX_REQLINE", (int)buffer[RX_REQLINE]);
		System.out.printf("%s\t: %d\n", "RX_ERROR", (int)buffer[RX_ERROR]);
		System.out.printf("%s\t: %d\n", "RX_CUR_OPR", (int)buffer[RX_CUR_OPR]);
		System.out.printf("%s\t: %d\n", "RX_SINKTEMPH", (int)buffer[RX_SINKTEMPH]);
		System.out.printf("%s\t: %d\n", "RX_SINKTEMPL", (int)buffer[RX_SINKTEMPL]);
		System.out.printf("%s\t\t: %d\n", "RX_KP_1", (int)buffer[RX_KP_1]);
		System.out.printf("%s\t\t: %d\n", "RX_KI_1", (int)buffer[RX_KI_1]);
		System.out.printf("%s\t\t: %d\n", "RX_KD_1", (int)buffer[RX_KD_1]);
		System.out.printf("%s\t: %d\n", "RX_SERIALH", (int)buffer[RX_SERIALH]);
		System.out.printf("%s\t: %d\n", "RX_SERIALL", (int)buffer[RX_SERIALL]);
		System.out.printf("%s\t: %d\n", "RX_SERIALRESERV", (int)buffer[RX_SERIALRESERV]);
		
		System.out.printf("%s\t: %d\n", "RX_VERSION", (int)buffer[RX_VERSION]);
		
		System.out.printf("%s\t: %d\n", "RX_TIME_1", (int)buffer[RX_TIME_1]);
		System.out.printf("%s\t: %d\n", "RX_TIME_2", (int)buffer[RX_TIME_2]);
		System.out.printf("%s\t: %d\n", "RX_TIME_3", (int)buffer[RX_TIME_3]);
		System.out.printf("%s\t: %d\n", "RX_TIME_4", (int)buffer[RX_TIME_4]);
		System.out.println( "-----------------------------" );
	}
}
