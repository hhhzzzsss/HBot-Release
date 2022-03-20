package com.github.hhhzzzsss.hbot.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.Logger;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.discord.DiscordUtils;

import lombok.*;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class LogQueryCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;
	
	private LogQueryThread queryThread = null;

	@Override
	public String getName() {
		return "logquery";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"[flags] count <query>",
			"[flags] report <query>",
			"cancel",
			"-ignorecase ...",
			"-regex ...",
		};
	}

	@Override
	public String getDescription() {
		return "Makes queries to HBot's log files";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		execute(args, PlatformInfo.getMinecraft(hbot, hbot.getCommandCore()));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args, PlatformInfo.getDiscord(hbot, event, "Log Statistics"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		List<String> flags = parser.readFlags();
		String subCommand = parser.readWord(true);
		try {
			if (subCommand.equalsIgnoreCase("count")) {
				countCommand(parser, flags, platform);
			} else if (subCommand.equalsIgnoreCase("report")) {
				reportCommand(parser, flags, platform);
			} else if (subCommand.equalsIgnoreCase("cancel")) {
				cancelCommand(parser, platform);
			} else {
				throw parser.getGenericError();
			}
		} catch (Exception e) {
			throw new CommandException(e.getMessage());
		}
	}
	
	private void countCommand(ArgsParser parser, List<String> flags, PlatformInfo platform) throws CommandException {
		if (queryThread != null && queryThread.isAlive()) {
			throw new CommandException("Another query is already running");
		}
		
		String query = parser.readString(true);
		platform.sendMessage(String.format("&7Counting instances of &3%s &7in logs...", query));
		queryThread = new CountThread(query, flags, platform);
		queryThread.start();
	}
	
	private void reportCommand(ArgsParser parser, List<String> flags, PlatformInfo platform) throws CommandException {
		if (queryThread != null && queryThread.isAlive()) {
			throw new CommandException("Another query is already running");
		}
		
		String query = parser.readString(true);
		platform.sendMessage(String.format("&7Collecting instances of &3%s &7in logs...", query));
		queryThread = new ReportThread(query, flags, platform);
		queryThread.start();
	}
	
	private void cancelCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		if (queryThread != null && queryThread.isAlive()) {
			queryThread.interrupt();
		}
		else {
			throw new CommandException("There is no query currently running");
		}
	}
	
	private abstract class LogQueryThread extends Thread {
		private PlatformInfo platform;
		
		protected boolean ignoreCase = false;
		protected boolean useRegex = false;
		
		protected String query;
		protected Pattern pattern = null;
		
		protected boolean breakFlag = false; // Used to stop scanning through logs in case the query is already finished.
		
		protected long startTime;
		
		protected LogQueryThread(String query, List<String> flags, PlatformInfo platform) {
			this.platform = platform;
			
			for (String flag : flags) {
				if (flag.equalsIgnoreCase("ignorecase")) {
					ignoreCase = true;
				}
				else if (flag.equalsIgnoreCase("regex")) {
					useRegex = true;
				}
			}
			
			this.query = query;
			if (useRegex) {
				if (ignoreCase) {
					pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
				}
				else {
					pattern = Pattern.compile(query);
				}
			}
			else {
				if (ignoreCase) {
					this.query = query.toLowerCase();
				}
			}
		}
		
		@Override
		public void run() {
			try {
				startTime = System.currentTimeMillis();
				
				if (!Logger.logDir.isDirectory()) {
					throw new RuntimeException("Logger.logDir is not a directory!");
				}
				
				File[] fileList = Logger.logDir.listFiles();
				Arrays.sort(fileList, (a, b) -> a.getName().compareTo(b.getName()));
				for (File file : fileList) {
					if (file.getName().matches("\\d\\d\\d\\d-\\d\\d-\\d\\d.txt.gz")) {
						LocalDate date = LocalDate.parse(file.getName().substring(0, 10));
						try (
								FileInputStream fin = new FileInputStream(file);
								GZIPInputStream gzin = new GZIPInputStream(fin, 65536);
								BufferedReader br = new BufferedReader(new InputStreamReader(gzin, StandardCharsets.UTF_8));
							) {
							br.readLine();
							readFile(br, date);
						}
					}
					else if (file.getName().equals("logFile.txt")) {
						try (
								FileInputStream fin = new FileInputStream(file);
								BufferedReader br = new BufferedReader(new InputStreamReader(fin, StandardCharsets.UTF_8));
							) {
							LocalDate date;
							try {
								date = LocalDate.parse(br.readLine());
							}
							catch (DateTimeParseException e) {
								date = LocalDate.now();
							}
							readFile(br, date);
						}
					}
					
					if (Thread.interrupted()) {
						sendMessageAsync("&7Log query cancelled");
						return;
					}
					else if (breakFlag) {
						break;
					}
				}
				onFinish();
			}
			catch (Exception e) {
				sendMessageAsync("&cError while querying logs: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		private void readFile(BufferedReader br, LocalDate date) throws IOException {
			String line;
			while ((line = br.readLine()) != null) {
				
				if (line.length() == 0) {
					continue;
				}
				
				// check for repetition suffix marked by notation like '[x21]'
				int repetitions = 1;
				int idx = line.length()-1;
				if (line.charAt(idx) == ']') {
					idx--;
					// Backtrack through digits of the repetition count (max 6 digits)
					while(idx >= line.length() - 7 && idx > 0 && isDigit(line.charAt(idx))) {
						idx--;
					}
				}
				// Check if there's a '[x' on the left side
				if (idx < line.length()-2 && idx > 0 && line.charAt(idx) == 'x' && line.charAt(idx-1) == '[') {
					repetitions = Integer.parseInt(line.substring(idx+1, line.length()-1));
					line = line.substring(0, idx-1);
				}
				processLine(line, repetitions);
				
				if (breakFlag) {
					return;
				}
			}
		}
		
		private boolean isDigit(char c) {
			return c >= '0' && c <= '9';
		}
		
		private void processLine(String line, int repetitions) {
			if (useRegex) {
				if (pattern.matcher(line).find()) {
					onMatch(line, repetitions);
				}
			}
			else {
				if (ignoreCase) {
					if (line.toLowerCase().contains(query)) {
						onMatch(line, repetitions);
					}
				}
				else {
					if (line.contains(query)) {
						onMatch(line, repetitions);
					}
				}
			}
		}
		
		protected abstract void onMatch(String line, int repetitions);
		protected abstract void onFinish();
		
		protected void sendMessageAsync(String message) {
			hbot.getExecutor().submit(() -> {
				platform.sendMessage(message);
			});
		}
		
		protected void breakQuery() {
			breakFlag = true;
		}
		
		protected long timeElapsed() {
			return System.currentTimeMillis() - startTime;
		}
	}
	
	private class CountThread extends LogQueryThread {
		private int count;
		
		public CountThread(String query, List<String> flags, PlatformInfo platform) {
			super(query, flags, platform);
		}
		
		@Override
		protected void onMatch(String line, int repetitions) {
			count += repetitions;
		}
		
		@Override
		protected void onFinish() {
			sendMessageAsync(String.format("&7Found &3%d &7matches for &3%s &7in &b%d seconds", count, query, timeElapsed() / 1000));
		}
	}
	
	private class ReportThread extends LogQueryThread {
		protected ReportThread(String query, List<String> flags, PlatformInfo platform) {
			super(query, flags, platform);
		}

		private StringBuilder results = new StringBuilder();
		private int numResults = 0;
		
		@Override
		protected void onMatch(String line, int repetitions) {
			results.append(line);
			if (repetitions > 1) {
				results.append(" [x" + Integer.toString(repetitions) + "]");
			}
			results.append("\n");
			numResults++;
			
			if (results.length() > 1000000) {
				breakQuery();
			}
		}
		
		@Override
		protected void onFinish() {
			if (results.length() > 1000000) {
				sendMessageAsync(String.format("&7Since the report was getting too large, the query, &3%s&7, was stopped after the first &3%d &7results in &b%d seconds&7. Results are sent in &bhbot-logs&7.", query, numResults, timeElapsed() / 1000));
			}
			else {
				sendMessageAsync(String.format("&7Found &3%d &7matches for &3%s &7in &b%d seconds&7. &7Results are sent in &bhbot-logs&7.", numResults, query, timeElapsed() / 1000));
			}
			
			long time = System.currentTimeMillis();
			for (TextChannel channel : hbot.getDiscordManager().logChannels) {
				channel.sendMessage(String.format("Log report for '%s'", DiscordUtils.sanitizeMentions(query))).addFile(results.toString().getBytes(), String.format("report-%d.txt", time)).queue();
			}
		}
	}
}
