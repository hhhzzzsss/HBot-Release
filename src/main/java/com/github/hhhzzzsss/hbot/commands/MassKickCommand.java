package com.github.hhhzzzsss.hbot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.command.*;

import lombok.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

@RequiredArgsConstructor
public class MassKickCommand implements GlobalDiscordCommand {
	private final Bot bot;

	@Override
	public String getName() {
		return "masskick";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"<regex>"};
	}

	@Override
	public String getDescription() {
		return "Kicks all discord guild members that match a particular regular expression";
	}

	@Override
	public int getPermission() {
		return 2;
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		event.getGuild().loadMembers()
			.onSuccess((members) -> {
				processMemberList(event, args, members);
			})
			.onError((e) -> {
				event.getMessage().reply("Error when querying for guild members: " + e.getMessage());
			});
	}
	
	public void processMemberList(MessageReceivedEvent event, String args, List<Member> members) {
		ArrayList<Member> matchingMembers = new ArrayList<>();
		for (Member m : members) {
			//System.out.println(m.getUser().getName());
			if (m.getUser().getName().matches(args)) {
				matchingMembers.add(m);
			}
		}
		
		if (matchingMembers.size() == 0) {
			event.getMessage().reply("Found no matching members");
			return;
		}

		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(100, 149, 237));
		eb.setTitle("Mass Kick");
		eb.setDescription(String.format("Kicking %d members...", matchingMembers.size()));
		StringBuilder sb = new StringBuilder();
		for (Member m : matchingMembers) {
			sb.append(m.getUser().getName() + "\n");
		}
		
		RestAction<?> restAction = event.getChannel().sendMessage(eb.build()).addFile(sb.toString().getBytes(), "kicked-players.txt");
		
		for (Member m : matchingMembers) {
			restAction = restAction.delay(2, TimeUnit.SECONDS).flatMap((o) -> m.kick());
		}
		
		long startingTime = System.currentTimeMillis();
		restAction = restAction.delay(2, TimeUnit.SECONDS).flatMap((o) -> {
			EmbedBuilder eb2 = new EmbedBuilder();
			eb2.setColor(new Color(100, 149, 237));
			eb2.setTitle("Mass Kick");
			eb2.setDescription(String.format("Finished kicking %d members after %.2f seconds", matchingMembers.size(), (System.currentTimeMillis() - startingTime) / 1000.0));
			return event.getChannel().sendMessage(eb2.build());
		});
		
		restAction.queue(
			(o) -> {System.out.println("success");},
			(e) -> e.printStackTrace()
		);
	}
}
