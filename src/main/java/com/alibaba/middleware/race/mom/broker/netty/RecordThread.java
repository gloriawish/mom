package com.alibaba.middleware.race.mom.broker.netty;

import com.alibaba.middleware.race.mom.broker.TaskManager;


/**
 * 记录发送速度的线程，同时控制重发线程的启动
 * @author zz
 *
 */
public class RecordThread implements Runnable {

	private int lastSpeed=0;
	private int nowSpeed=0;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(true)
		{
			lastSpeed=ResendManager.getSendSpeed();
			ResendManager.record();
			
			nowSpeed=ResendManager.getSendSpeed();
			
			//如果有重发数据，且重发线程为0 则启动重发线程
			if(TaskManager.getResendNumber()>0&&ResendManager.resendThreadNumber==0)
			{
				System.out.println("start resend Thread");
				ResendManager.startResend();
			}
			
			if(lastSpeed!=0)
			{
				float per=(float)(nowSpeed/lastSpeed);
				if(per<1&&ResendManager.resendThreadNumber!=0)//发送速率降低的时候。判断一下是否需要增加重发线程
				{
					//重发数量/重发线程数 比值大于1000 则增加发送线程
					if((TaskManager.getResendNumber()/ResendManager.resendThreadNumber)>1000)
					{
						System.out.println("add resend Thread");
						ResendManager.startResend();
					}
				}
			}
			try {
				Thread.sleep(ResendManager.recordTime*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
