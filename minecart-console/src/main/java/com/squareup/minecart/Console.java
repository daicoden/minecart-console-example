package com.squareup.minecart;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jruby.embed.ScriptingContainer;
import org.jruby.util.Dir;

import static com.google.common.base.Throwables.propagate;
import static java.lang.System.getenv;
import static org.jruby.CompatVersion.RUBY1_9;
import static org.jruby.embed.AttributeName.BASE_DIR;
import static com.squareup.minecart.ConsoleOptions.buildOptions;

/**
 * Console and ConsoleOptions are gutted from a larger initialization process in minecart.
 */
public class Console {
  private final ScriptingContainer ruby;

  public Console(ConsoleOptions options) {
    ruby = new ScriptingContainer();
    ruby.setAttribute(BASE_DIR, options.getWorkingDirectory());
    ruby.setCompatVersion(RUBY1_9);
    ruby.setCurrentDirectory(options.getWorkingDirectory());
    ruby.setEnvironment(options.getEnvironment());
    ruby.setLoadPaths(options.getLoadPaths());
  }

  public void run() {
    ruby.runScriptlet("require 'irb'");
    ruby.runScriptlet("IRB.start");
  }

  public static void main(String... args) throws Exception {
    new Console(buildOptions(args)).run();
  }
}
