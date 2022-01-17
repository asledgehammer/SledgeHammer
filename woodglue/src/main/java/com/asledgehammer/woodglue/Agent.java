package com.asledgehammer.woodglue;

import java.lang.instrument.Instrumentation;

/**
 * Java-Agent to handle WoodGlue's assembly of the ProjectZomboid environment for Sledgehammer.
 *
 * @author Jab
 */
public class Agent {

  /**
   * Premain entry point for Sledgehammer. Handles the pre-main operations for WoodGlue.
   *
   * @param args The Java arguments.
   * @param inst The Java Instrumentation Object passed to the pre-main Agent exclusively.
   */
  public static void premain(String args, Instrumentation inst) {
    new WoodGlue(args, inst);
  }
}
