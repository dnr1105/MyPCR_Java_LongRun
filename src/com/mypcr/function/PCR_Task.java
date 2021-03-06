package com.mypcr.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;

import javax.swing.JOptionPane;

import com.hidapi.DeviceConstant;
import com.mypcr.beans.Action;
import com.mypcr.beans.RxAction;
import com.mypcr.beans.State;
import com.mypcr.beans.State_Oper;
import com.mypcr.beans.TxAction;
import com.mypcr.handler.Handler;
import com.mypcr.timer.GoTimer;
import com.mypcr.timer.NopTimer;
import com.mypcr.ui.ButtonUI;
import com.mypcr.ui.MainUI;
import com.mypcr.ui.ProgressDialog;

public class PCR_Task 
{
	private static PCR_Task instance = null;
	
	// 다른 객체의 정보들
	private static MainUI m_MainUI = null;
	
	// 에러 메시지 종류
	private static final int ERROR_LID_OVER			 = 0x01;
	private static final int ERROR_CHM_OVER			 = 0x02;
	private static final int ERROR_LID_CHM_OVER		 = 0x03;
	private static final int ERROR_HEATSINK_OVER	 = 0x04;
	private static final int ERROR_LID_HEATSINK_OVER = 0x05;
	private static final int ERROR_CHM_HEATSINK_OVER = 0x06;
	private static final int ERROR_ALL				 = 0x07;
	private static final int ERROR_TEMP_SENSOR		 = 0x08;
	private static final int ERROR_ALL_SYSTEM		 = 0x0f;
	
	private static final int deltaTemp	= 50;
	private static final int _deltaTemp	= 95;
	
	// Timer 를 사용하기 위한 객체
	private Timer m_NopTimer = null;
	private Timer m_GoTimer  = null;
	
	// Rx, Tx 버퍼를 생성해준다.
	public RxAction		m_RxAction = null;
	public TxAction		m_TxAction = null;
	
	// PCR 관련 변수들
	public int LED_Counter = 0;
	public int List_Counter = 0;
	public int Timer_Counter = 0;
	public int m_nCur_ListNumber = 0;
	
	// PCR 관련 플래그들
	public boolean IsRunning = false;
	public boolean IsReadyToRun = true;
	public boolean IsFinishPCR	 = false;
	public boolean IsRefrigeratorEnd = false;
	public boolean IsProtocolEnd = false;
	public boolean IsAdmin = false;
	public boolean IsGotoStart = false;
	private boolean IsDeviceCheck = false;
	private boolean isComplete = false;
	
	// Preheat
	private String m_Preheat = "104";
	
	private PCR_Task()
	{
		m_RxAction = new RxAction();
		m_TxAction = new TxAction();
	}
	
	public static PCR_Task getInstance(MainUI mainUI)
	{
		if( instance == null )
		{
			m_MainUI = mainUI;
			instance = new PCR_Task();
		}
		return instance;
	}
	
	public void setTimer(int timer)
	{
		switch( timer )
		{
			case NopTimer.TIMER_NUMBER:
				m_NopTimer = new Timer("Nop Timer");
				m_NopTimer.schedule(new NopTimer( m_MainUI ), Calendar.getInstance().getTime(), NopTimer.TIMER_DURATION);
				break;
			case GoTimer.TIMER_NUMBER:
				m_GoTimer = new Timer("Go Timer");
				m_GoTimer.schedule( new GoTimer( m_MainUI.getDevice(), m_MainUI.getActionList(), m_Preheat, m_MainUI ), Calendar.getInstance().getTime(), GoTimer.TIMER_DURATION);
				break;
		}
	}
	
	public void killTimer(int timer)
	{
		switch( timer )
		{
			case NopTimer.TIMER_NUMBER:
				if( m_NopTimer != null ){
					m_NopTimer.cancel();
					m_NopTimer = null;
				}
				break;
			case GoTimer.TIMER_NUMBER:
				if( m_GoTimer != null ){
					m_GoTimer.cancel();
					m_GoTimer = null;
				}
				break;
		}
	}
	
	// ju
	private long startTime = 0;
	private long currentTime = 0;
	private long delta = 0;
	private long _delta = 0;
	private int longRunCount = 0;
	
	public boolean isLongRunTestMode = false;
	public boolean longRunFlag = false;
	public boolean longRunRealFinish = true;
	
	int runTime = 0;
	int targetCount = 0;
	boolean startFlag = true;
	boolean currentFlag = false;
	boolean isDelta = true; // true = d, false = _d
	boolean runReady = false;
	boolean newStart = true;
	
	public static double toDeltaTemp = ( (_deltaTemp - deltaTemp) * 0.1 + deltaTemp );
	public static double to_DeltaTemp = ( (_deltaTemp - deltaTemp) * 0.9 + deltaTemp );
	
	public void Calc_Temp()
	{
		double Chamber_Temp, Heater_Temp;
		Chamber_Temp = (double)(m_RxAction.getChamber_TempH()) + (double)(m_RxAction.getChamber_TempL()) * 0.1;
		Heater_Temp = (double)(m_RxAction.getCover_TempH()) + (double)(m_RxAction.getCover_TempL()) * 0.1;
		String chamber = String.format("%4.1f ℃", Chamber_Temp);
		String heater = String.format("%4.1f ℃", Heater_Temp);
		m_MainUI.getStatusText().setMessage(chamber, 1);
		m_MainUI.getStatusText().setMessage(heater, 2);
		
		if( isDelta && newStart )
		{
			// 첫 온도값보다 온도가 높으면 StartTime이 기록되지 않는 것을 방지하기 위함 (+-2)
			if( Chamber_Temp <= deltaTemp + 1)
			{
				System.out.println( "1. Flag in: "+ Chamber_Temp);
				// 첫 설정 온도보다 낮다면 runReady flag를 시작
				startTime = 0;
				currentTime = 0;
				++longRunCount;
				runReady = true;
				newStart = false;
			}
			else
			{
				runReady = false;
			}
		}
		
		// delta, _delta를 구분하기 위한 플래그 isDelta
		if( isDelta && runReady )
		{
			// 온도값이 여러번 기록되는 것과 StartTime을 기록하기 위함
			// 17.5 이상, 19.5 이하 (+-1)
			if( ( ( Chamber_Temp >= toDeltaTemp - 1 ) && ( Chamber_Temp <= toDeltaTemp + 1 ) ) && startFlag )
			{
				startTime = System.currentTimeMillis( );
				startFlag = !startFlag;
				currentFlag = !currentFlag;
//				Functions.log( "[0] startTime = " + startTime );
				System.out.println( "[0] startTime = " + startTime + ", " + Chamber_Temp );
			}
			// 85.5 이상, 87.5 이하 (+-1)
			else if( ( ( Chamber_Temp <= to_DeltaTemp + 1 ) && ( Chamber_Temp >= to_DeltaTemp - 1 ) ) && currentFlag )
			{
				currentTime = System.currentTimeMillis( );
				currentFlag = !currentFlag;
				startFlag = !startFlag;
//				Functions.log( "[0] currentTime = " + currentTime );
				System.out.println( "[0] currentTime = " + currentTime + ", " + Chamber_Temp );
				delta = currentTime - startTime;
//				Functions.log( String.format( "# Auto Run count: %d(delta=%d)\n", longRunCount, delta) );
				System.out.println( String.format( "# Auto Run count: %d(delta=%d)\n", longRunCount, delta ) );
//				Functions.log(  String.format( "%10d\t%10d", longRunCount, delta ) );
				isDelta = !isDelta;
				newStart = true;
				runReady = false;
			}
		}
		
		if( !isDelta && newStart )
		{
			// 온도가 95도까지 미처 도달하지 못했을때를 방지
			if( Chamber_Temp >= _deltaTemp - 1)
			{
				System.out.println( "2. Flag in: "+ Chamber_Temp);
				startTime = 0;
				currentTime = 0;
				runReady = true;
				newStart = false;
			}
			else
			{
				runReady = false;
			}
		}
		
		if( !isDelta && runReady )
		{
			// 85.5 이상, 87.5 이하 (+-1)
			if( ( ( Chamber_Temp <= to_DeltaTemp + 1 ) && ( Chamber_Temp >= to_DeltaTemp - 1 ) ) && startFlag )
			{
				startTime = System.currentTimeMillis( );
				startFlag = !startFlag;
				currentFlag = !currentFlag;
//				Functions.log( "[1] startTime = " + startTime );
				System.out.println( "[1] startTime = " + startTime + ", " + Chamber_Temp );
			}
			// 17.5 이상, 19.5 이하 (+-1)
			else if( ( ( Chamber_Temp >= toDeltaTemp - 1 ) && ( Chamber_Temp <= toDeltaTemp + 1 ) ) && currentFlag )
			{
				currentTime = System.currentTimeMillis( );
				startFlag = !startFlag;
				currentFlag = !currentFlag;
//				Functions.log( "[1] currentTime = " + currentTime );
				System.out.println( "[1] currentTime = " + currentTime + ", " + Chamber_Temp );
				_delta = currentTime - startTime;
				System.out.println( String.format( "# Auto Run count: %d(delta=%d)\n", longRunCount, _delta ) );
//				Functions.log(  String.format( "%10d\t%10d", longRunCount, delta ) );
				if(longRunCount%10==0) Functions.log(  String.format( "%10d\t%10d\t%10d", longRunCount, delta, _delta ) );
				isDelta = !isDelta;
				newStart = true;
				runReady = false;
			}
		}
	}
	
	public void Check_Status()
	{
		switch( m_RxAction.getState() )
		{
			case State.READY:
				switch( m_RxAction.getCurrent_Operation() )
				{
					case State_Oper.INIT:
						m_MainUI.bLEDOff();
						break;
					case State_Oper.COMPLETE:
						m_MainUI.bLEDOn();
						if( !IsReadyToRun )
						{
							IsFinishPCR = true;
							IsReadyToRun = true;
							isComplete = true;
							PCR_End();
						}
						break;
					case State_Oper.INCOMPLETE:
						m_MainUI.bLEDOff();
						m_MainUI.rLEDOn();
						break;
				}
				break;
			case State.RUN:
				m_MainUI.rLEDOff();
				if( m_RxAction.getCurrent_Operation() == State_Oper.RUN_REFRIGERATOR )
				{
					m_MainUI.bLEDOn();
					IsRefrigeratorEnd = true;
					IsProtocolEnd = true;
					IsFinishPCR = true;
				}
				else
				{
					if( LED_Counter > 8 )
						m_MainUI.bLEDOn();
					else if( LED_Counter == 0 )
						m_MainUI.bLEDOff();
				}
				LED_Counter++;
				
				if( LED_Counter == 14 )
					LED_Counter = 0;
				IsReadyToRun = false;
				break;
			case State.PCREND:
				m_MainUI.bLEDOn();
				break;
		}
	}
	
	public void Line_Task()
	{
		int taskLabel, Action_Point = 0;
		String tempString;
		Action[] actions = m_MainUI.getActionList();
		if( actions == null )
			return;
		int lines = actions.length;
		
		m_nCur_ListNumber = (int)m_RxAction.getCurrent_Action() - 1;
		
		if( List_Counter > 5 )
		{
			for(int i=0; i<lines; i++)
			{
				tempString = actions[i].getLabel();
				if( !tempString.equals("GOTO") )
				{
					taskLabel = Integer.parseInt(tempString);
					if( taskLabel == m_nCur_ListNumber + 1)
					{
						Action_Point = i;
						break;
					}
				}
			}
			
			m_nCur_ListNumber = Action_Point;
			
			if( IsRunning )
			{
				Display_LineTime();
				
				// Select 처리
				m_MainUI.getProtocolList().setSelection(m_nCur_ListNumber);
			}
			List_Counter = 0;
		}
		else
			List_Counter++;
	}
	
	public void Display_LineTime()
	{
		int durs, durm;
		String tempString;
		Action[] actions = m_MainUI.getActionList();
		
		tempString = actions[m_nCur_ListNumber].getLabel();
		
		if( tempString.equals("GOTO") )
			m_MainUI.getProtocolList().ChangeRemainTime("", m_nCur_ListNumber-1);
		else
			m_MainUI.getProtocolList().ChangeRemainTime("", m_nCur_ListNumber);
		
		durs = (int)m_RxAction.getSec_TimeLeft();
		durm = durs/60;
		durs = durs%60;
		
		if( durs == 0 )
		{
			if( durm == 0 ) tempString = "";
			else tempString = durm + "m";
		}
		else
		{
			if( durm == 0 ) tempString = durs + "s";
			else tempString = durm + "m " + durs + "s";
		}
		
		// 모든 라인의 남은 시간 초기화(GOTO 부분 제외)
		for(int i=0; i<m_RxAction.getTotal_Action(); i++)
		{
			if( !actions[i].getLabel().equals("GOTO"))
				m_MainUI.getProtocolList().ChangeRemainTime("", i);
		}
		
		// 남은 시간 변경
		m_MainUI.getProtocolList().ChangeRemainTime(tempString, m_nCur_ListNumber);
				
		if( m_RxAction.getCurrent_Loop() != 0 )
		{
			if( m_RxAction.getCurrent_Loop() == 255 )
				IsGotoStart = true;
		}
		
		if( m_RxAction.getCurrent_Loop() != 255 )
		{
			if( IsGotoStart )
			{
				boolean flag = true;
				for( int i=m_nCur_ListNumber; i<m_RxAction.getTotal_Action(); i++ )
				{
					tempString = actions[i].getLabel();
					if( tempString.equals("GOTO") )
					{
						if( flag )
						{
							flag = false;
							tempString = m_RxAction.getCurrent_Loop() + "";
							m_MainUI.getProtocolList().ChangeRemainTime(tempString, i);
						}
					}
				}
			}
		}
	}
	
	public void Get_DeviceProtocol()
	{
		if( !IsDeviceCheck && m_MainUI.IsNoStop )
		{
			IsDeviceCheck = true;
			
			// 액션이 없을 경우에, 즉 처음 켰을 때
			if( m_RxAction.getTotal_Action() == 0 )
			{
				// Recent Protocol 파일 경로를 받아온다.
				String path = Functions.Get_RecentProtocolPath();
				
				Functions.log("최근 Protocol File 읽기 시도");
				
				// 최근 불러온 파일이 있을 경우
				if( path != null )
				{
					Action[] actions = null;
					try
					{
						actions = Functions.loadProtocol(path);
					}catch(Exception e)
					{
						Functions.log("최근 Protocol File 읽기 실패(설정된 Path 값이 지워짐)");
						JOptionPane.showMessageDialog(null, "No Recent Protocol File! Please Read Protocol!");
						return;
					}
					
					Functions.log("최근 Protocol File 읽기 완료");
					
					m_MainUI.OnHandleMessage(Handler.MESSAGE_READ_PROTOCOL, actions);
				}
				else{
					Functions.log("최근 Protocol File 읽기 실패(존재하지 않음)");
					JOptionPane.showMessageDialog(null, "No Recent Protocol File! Please Read Protocol!");
				}
				return;
			}

			byte readLine = 0;
			int reqline = 0;
			ArrayList<Action> actions = new ArrayList<Action>();
			final ProgressDialog dialog = new ProgressDialog(m_MainUI, "Checking the state of the equipment", (int)m_RxAction.getTotal_Action());
			Thread tempThread = new Thread()
			{
				public void run()
				{
					dialog.setModal(true);
					dialog.setVisible(true);
				}
			};
			tempThread.start();
			
			// 장비로부터 얻은 Protocol 로 PCR 을 시작하는 경우에도 Log 를 쓰도록
			Functions.setLogging(true);
			
			Functions.log("장비로부터 Protocol 을 읽기 시도");

			while( readLine < (int)m_RxAction.getTotal_Action() )
			{
				dialog.setProgressValue(readLine);
				try
				{
					m_MainUI.getDevice().write( m_TxAction.Tx_RequestLine(readLine) );
					
					try
					{
						Thread.sleep(10);
					}catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					
					byte[] readBuffer = new byte[65];
					
					Functions.log(String.format("장비로부터 Protocol 을 읽기 시도(%d/%d)", readLine+1, m_RxAction.getTotal_Action()));
					
					if( m_MainUI.getDevice().read(readBuffer) != 0 )
					{
						RxAction tempAction = new RxAction();
						tempAction.set_Info(readBuffer);
						
						reqline = tempAction.getReqLine();
						m_RxAction.setTotal_Action( tempAction.getTotal_Action() );
						
						if( reqline == readLine )
						{
							Action action = new Action("Device Protocol");
							if( (tempAction.getLabel()) != RxAction.AF_GOTO )
								action.setLabel("" + tempAction.getLabel());
							else
								action.setLabel("GOTO");
							
							action.setTemp("" + tempAction.getTemp());
//							int time = ((int)(tempAction.getTime_H()*256.) + (int)(tempAction.getTime_L()));
							int time = ((int)(tempAction.getTime_1( ) * 16777216.)
									+(int)(tempAction.getTime_2( ) * 65536.)
									+(int)(tempAction.getTime_3( ) * 256.)
									+(int)(tempAction.getTime_4( )));
							action.setTime("" + time);
							
							actions.add(action);
							Functions.log(String.format("장비로부터 Protocol 을 읽기 성공(%d/%d) Label: %s, Temp: %s, Time: %s", 
									readLine+1, m_RxAction.getTotal_Action(), action.getLabel(), action.getTemp(), action.getTime()));
							readLine++;
						}
						else{
							Functions.log(String.format("장비로부터 Protocol 을 읽기 시도 실패(%d/%d)", readLine+1, m_RxAction.getTotal_Action()));
						}
					}
					
					if( readLine == m_RxAction.getTotal_Action() )
					{
						IsRunning = true;
						IsFinishPCR = false;
						
						m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_START, false);
						m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_STOP, true);
						m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_PROTOCOL, false);
					}
					
				}catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			
			// 리스트에 action 을 뿌려주기 위해..
			Action[] tempAction = new Action[actions.size()];
			for(int i=0; i<tempAction.length; i++)
			{
				tempAction[i] = new Action("Device Protocol");
				tempAction[i].setLabel(actions.get(i).getLabel());
				tempAction[i].setTemp(actions.get(i).getTemp());
				tempAction[i].setTime(actions.get(i).getTime());
			}
			m_MainUI.OnHandleMessage(Handler.MESSAGE_READ_PROTOCOL, tempAction);
			
			Thread tempThread2 = new Thread()
			{
				public void run()
				{
					try
					{
						Thread.sleep(1000);
					}catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					dialog.dispose();
				}
			};
			tempThread2.start();
		}
	}
	
	public void Error_Check()
	{
		// 에러가 있다면
		if( m_RxAction.getError() != 0 )
		{
			Print_ErrorMsg( m_RxAction.getError() );
		}
	}
	
	public void Print_ErrorMsg(int error)
	{
		String message = "";
		
		switch( error )
		{
			case ERROR_LID_OVER:
				message = "LID overheating error! Please power-off and check MyPCR machine!";
				break;
			case ERROR_CHM_OVER:
				message = "Chamber overheating error! Please power-off and check MyPCR machine!";
				break;
			case ERROR_LID_CHM_OVER:
				message = "LID Heater and Chamber overheating error! Please power-off and check MyPCR machine!";
				break;
			case ERROR_HEATSINK_OVER:
				message = "Heat Sink overheating error! Please power-off and check MyPCR machine!";
				break;
			case ERROR_LID_HEATSINK_OVER:
				message = "LID Heater and Heat Sink overheating error! Please power-off and check MyPCR machine!";
				break;
			case ERROR_CHM_HEATSINK_OVER:
				message = "Chamber and Heat Sink overheating error! Please power-off and check MyPCR machine!";
				break;
			case ERROR_ALL:
				message = "LID Heater and Chamber, Heat Sink overheating error! Please power-off and check MyPCR machine!";
				break;
			case ERROR_TEMP_SENSOR:
				message = "Temperature Sensor error! Please power-off and check MyPCR machine!";
				break;
			case ERROR_ALL_SYSTEM:
				message = "All system is not working! Please power-off and check MyPCR machine!";
				break;
		}
		
		Functions.log("에러 발생: " + message);
		JOptionPane.showMessageDialog(null, message);
	}
	
	public void Calc_Time()
	{
		int hour, minute, second;
		long totalTime = m_RxAction.getTotal_TimeLeft();
//		System.out.println( "totalTime: "+totalTime );
//		System.out.println( "LEFTTIME Q: "+m_RxAction.getLefttime_Q( ) );
//		System.out.println( "LEFTTIME H: "+m_RxAction.getLefttime_H( ) );
//		System.out.println( "LEFTTIME L: "+m_RxAction.getLefttime_L( ) );
//		System.out.println( "rx_time_3: "+m_RxAction.ki( ) );
		
		switch( m_RxAction.getState() )
		{
			case State.READY:
				if( IsRunning )
				{
					IsRunning = false;
					m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_START, true);
					m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_STOP, false);
					m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_PROTOCOL, true);
				}
				break;
			case State.RUN:
				IsRunning = true;
				m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_START, false);
				m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_STOP, true);
				m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_PROTOCOL, false);
				break;
		}
		
		Timer_Counter++;
		
		if( Timer_Counter % 5 == 0 )
		{
			second = (int)( totalTime % 60 );
			minute = (int)( totalTime / 60 );
			hour = minute / 60;
			minute = minute - hour * 60;
			
			if( IsRunning && totalTime != 0 )
				m_MainUI.getProtocolText().setRemainingTimeText((hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second));
		}
		
		if( Timer_Counter == 10 )
			Timer_Counter = 0;
	}
	
	public void PCR_Start(String preheat)
	{
		IsRunning = true;
		IsFinishPCR = false;
		IsGotoStart = false;
		m_Preheat = preheat;
		int lines = m_MainUI.getActionList().length;
		for(int i=0; i<lines; i++)
			m_MainUI.getProtocolList().ChangeRemainTime("", i);
	}
	
	public void Stop_PCR()
	{
		if( IsRunning )
		{
			m_NopTimer.cancel();
			try
			{
				Thread.sleep(300);
			}catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			try
			{
				m_MainUI.getDevice().write( m_TxAction.Tx_Stop() );
			}catch(IOException e)
			{
				System.err.println( e );
			}
			try
			{
				Thread.sleep(300);
			}catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			
			if( !IsProtocolEnd )
			{
				m_MainUI.rLEDOn();
				IsRunning = false;
				IsFinishPCR = false;
			}
			else if( IsRefrigeratorEnd )
			{
				m_MainUI.rLEDOff();
				IsFinishPCR = true;
				IsRunning = false;
				setTimer(NopTimer.TIMER_NUMBER);
				return;
			}
			
			setTimer(NopTimer.TIMER_NUMBER);
		}
	}
	
	public void PCR_End()
	{
		m_nCur_ListNumber = 0;
		IsRunning = false;
		
		m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_START, true);
		m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_STOP, false);
		m_MainUI.getButtonUI().setEnable(ButtonUI.BUTTON_PROTOCOL, true);
		
		Functions.log("PCR 종료!(" + (IsFinishPCR ? "PCR Ended!!" : "PCR Incomplete!!") + ")");
		Functions.setLogging(false);
		
		if( IsFinishPCR )
		{
			int lines = m_MainUI.getActionList().length;
			for(int i=0; i<lines; i++)
				m_MainUI.getProtocolList().ChangeRemainTime("", i);
			m_MainUI.getProtocolList( ).clearSelection( );
			if( !m_MainUI.isTestMode( ) && isComplete )
			{
				isComplete = false;
				JOptionPane.showMessageDialog( null, "PCR Ended!!", m_MainUI.getSerialNumber( ), JOptionPane.OK_OPTION );
			}
		}
		else
		{
			if( !m_MainUI.isTestMode( ) ) JOptionPane.showMessageDialog( null, "PCR Incomplete!!",
					m_MainUI.getSerialNumber( ), JOptionPane.OK_OPTION );
		}
	}
}
