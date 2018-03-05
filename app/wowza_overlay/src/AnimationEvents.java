//package com.wowza.wms.plugin.transcoderoverlays;

import java.util.ArrayList;
import java.util.List;

public class AnimationEvents extends Object
{
	private List<AnimationOperation> AnimationSteps = new ArrayList<AnimationOperation>();
	private AnimationOperation curAnimationOperation=null;
	private int curAnimationOpIdx=-1;
	private int lastStepValue = 0;
	
	/**
	 * Increments the currentStep value
	 * @return - <b>True</b> if there is an animation that got stepped and it is <u>not</u> a non-operation.<br>- <B>False</b> otherwise.
	 */
	public boolean step()
	{
		if(setCurrentAnimationOperation())
		{
			curAnimationOperation.currentStep++;			
		}
		return curAnimationOperation!=null && !curAnimationOperation.no_op;

	}
	
	/**
	 * 
	 * @return - the current value for the animation based on the start/end and step.
	 */
	public int getStepValue()
	{
		if(curAnimationOperation!=null && !curAnimationOperation.no_op)
		{
			int deltaSteps = curAnimationOperation.end - curAnimationOperation.start;
			double opPercent = (double)curAnimationOperation.currentStep/(double)curAnimationOperation.steps;
			double stepValue = curAnimationOperation.start + ((int)(deltaSteps*opPercent));
			lastStepValue = (int) Math.round(stepValue);
		}
		return lastStepValue;
	}
	/**
	 * Adds an animation step as a non-operation.
	 * @param noop_steps - number of steps to do nothing.
	 */
	public void addAnimationStep(double noop_steps)
	{
		AnimationOperation ao = new AnimationOperation(noop_steps);
		if(curAnimationOperation==null)
		{
			curAnimationOperation=ao;
			curAnimationOpIdx = 0;		
		}
		AnimationSteps.add(ao);
	}
 	/**
 	 * Adds an animation step.
 	 * @param start - starting value for the steps to take.
 	 * @param end - ending value for the steps to take.
 	 * @param steps - number of steps it will take to get from <b>start</b> to <b>end</b>.
 	 */
	public void addAnimationStep(int start, int end, double steps)
	{
		AnimationOperation ao = new AnimationOperation(start,end,steps);
		if(curAnimationOperation==null)
		{
			curAnimationOperation=ao;
			curAnimationOpIdx = 0;		
		}
		AnimationSteps.add(ao);
	}
	private boolean setCurrentAnimationOperation()
	{
		if(curAnimationOperation!=null)
		{
			if(curAnimationOperation.currentStep >= curAnimationOperation.steps)
			{
				//update to next one in list
				curAnimationOperation.currentStep=0;
				curAnimationOpIdx++;
				if(curAnimationOpIdx >=AnimationSteps.size())
				{
					curAnimationOpIdx=0;
				}
				curAnimationOperation = AnimationSteps.get(curAnimationOpIdx);
				curAnimationOperation.currentStep=0;
			}
		}
		return curAnimationOperation!=null;
	}

	public class AnimationOperation extends Object
	{
		boolean no_op = false;
		int start;			//start value
		int end;			//end value
		double steps;		//number to "steps between start and end
		int currentStep;	//current step value
		
		public AnimationOperation(double noop_stp)
		{
			no_op=true;
			steps=noop_stp;
			currentStep=0;
		}
		public AnimationOperation(int st, int ed, double stp)
		{
			no_op=false;
			start=st;
			end=ed;
			steps=stp;
			currentStep=0;
		}
	}
}