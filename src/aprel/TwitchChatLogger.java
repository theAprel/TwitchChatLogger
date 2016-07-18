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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

/**
 *
 * @author Aprel
 */
public class TwitchChatLogger extends PircBot {
    
    private static final String IRC_SERVER = "irc.chat.twitch.tv";
    private static final int IRC_PORT = 6667;
    
    private final String nick;
    private final String oauth;
    private final String channel;
    private final Writer writer;
    
    public TwitchChatLogger(String nick, String oauth, String channel) 
            throws IOException {
        this.nick = nick;
        this.oauth = oauth;
        this.channel = channel;
        writer = new FileWriter(new File(channel + ".txt"), true);
    }
    
    public void connect() throws IOException, IrcException {
        setName(nick);
        connect(IRC_SERVER, IRC_PORT, oauth);
        joinChannel(channel);
    }

    @Override
    protected void onMessage(String channel, String sender, String login, 
            String hostname, String message) {
        
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
