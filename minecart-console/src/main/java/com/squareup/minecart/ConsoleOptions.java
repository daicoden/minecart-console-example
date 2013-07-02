package com.squareup.minecart;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.lang.System.getenv;

public class ConsoleOptions {
  @Parameter(names = {"--with-system-gems", "-w"}, description = "Include System Gems.")
  private Boolean withSystemGems = false;

  private Settings settings;

  private ConsoleOptions() {}

  public static ConsoleOptions buildOptions(String[] args) throws Exception {
    ConsoleOptions options = new ConsoleOptions();

    parseOptions(args, options);

    if (options.withSystemGems) {
      options.settings = new SystemGemSettings();
    } else {
      String extractedGems = new GemExtractor().extractGems();
      options.settings = new PackagedGemSettings(extractedGems);
    }

    return options;
  }

  private static void parseOptions(String[] args, ConsoleOptions options) {
    JCommander parser = new JCommander(options);
    parser.setProgramName(Console.class.getSimpleName());

    try {
      parser.parse(args);
    } catch (ParameterException e) {
      StringBuilder buffer = new StringBuilder();
      parser.usage(buffer);
      System.err.println(buffer);
      System.exit(1);
    }
  }

  Map<String, String> getEnvironment() {
    return ImmutableMap.<String, String>builder()
        .put("GEM_HOME", settings.gemHome())
        .put("GEM_PATH", settings.gemPath())
        .build();
  }

  List<String> getLoadPaths() {
    return settings.loadPaths();
  }

  String getWorkingDirectory() {
    String path = jarPath();
    return path.substring(0, path.lastIndexOf("/"));
  }

  public static abstract class Settings {
    abstract String gemHome();
    abstract String gemPath();
    abstract List<String> loadPaths();
  }

  static class SystemGemSettings extends Settings {
    @Override String gemHome() {
      return getenv("GEM_HOME");
    }

    @Override String gemPath() {
      return getenv("GEM_PATH");
    }

    @Override List<String> loadPaths() {
      return ImmutableList.of();
    }
  }

  static class PackagedGemSettings extends Settings {
    private final String extractedGemDirectory;

    public PackagedGemSettings(String extractedGemDirectory) {

      this.extractedGemDirectory = extractedGemDirectory;
    }

    @Override String gemHome() {
      return extractedGemDirectory;
    }

    @Override String gemPath() {
      return extractedGemDirectory;
    }

    @Override List<String> loadPaths() {
      return ImmutableList.of();
    }
  }

  // http://stackoverflow.com/questions/2837263/how-do-i-get-the-directory-that-the-currently-executing-jar-file-is-in
  private static String jarPath() {
    URL url = Console.class.getProtectionDomain().getCodeSource().getLocation();
    return url.getPath();
  }
}