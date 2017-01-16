/*
 * Copyright (C) 2016 Aprel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package aprel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.time.Instant;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;


/**
 *
 * @author Aprel
 */
public class TwitchChatLogger extends ListenerAdapter {
    
    private static final String IRC_SERVER = "irc.chat.twitch.tv";
    private static final int IRC_PORT = 6667;
    
    private final String nick;
    private final String oauth;
    private final String channel;
    private final Writer writer;
    
    public TwitchChatLogger(String nick, String oauth, String channel) 
            throws IOException {
        this.nick = nick;
        this.oauth = oauth.startsWith("oauth:") ? oauth : "oauth:" + oauth;
        this.channel = channel;
        writer = new OutputStreamWriter(new FileOutputStream(
                new File(channel + ".txt"), true), Charset.forName("UTF-8"));
    }
    
    public void connect() throws IOException, IrcException {
        Configuration config = new Configuration.Builder()
                .setAutoNickChange(false) //Twitch doesn't support multiple users
                .setOnJoinWhoEnabled(false) //Twitch doesn't support WHO command
                .setCapEnabled(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership")) //Twitch by default doesn't send JOIN, PART, and NAMES unless you request it, see https://github.com/justintv/Twitch-API/blob/master/IRC.md#membership

                .addServer("irc.twitch.tv")
                .setName(this.nick) //Your twitch.tv username
                .setServerPassword(this.oauth) //Your oauth password from http://twitchapps.com/tmi
                .addAutoJoinChannel(this.channel) //Some twitch channel
                .addListener(this)
                .buildConfiguration();
        PircBotX bot = new PircBotX(config);
        bot.startBot();
    }

    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {
        Instant now = Instant.now();
        StringBuilder logline = new StringBuilder();
        String sender = event.getUser().getNick();
        String message = event.getMessage();
        logline.append(now).append(" ").append(sender).append(": ").append(message)
                .append(System.lineSeparator());
        try {
            writer.write(logline.toString());
            writer.flush();
        }
        catch(IOException ex) {
            System.err.println("Error writing to file. Program will terminate.");
            ex.printStackTrace();
        }
    }
    
    public static void main(String args[]) throws Exception {
        if(args == null || args.length != 3) {
            System.out.println("java -jar TwitchChatLogger.jar <Twitch username>"
                    + " <Twitch OAuth> <channel>");
            System.out.println("Don't forget the \"#\" symbol before the channel.");
            System.exit(0);
        }
        
        TwitchChatLogger logger = new TwitchChatLogger(args[0], args[1], args[2]);
        logger.connect();
    }
}
