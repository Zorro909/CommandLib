 package de.Zorro909.CommandLib.Discord;
 
 import de.Zorro909.CommandAPI.Abstractions.AbstractMessageChannel;
 import de.Zorro909.CommandAPI.Abstractions.AbstractUser;
 import net.dv8tion.jda.api.Permission;
 import net.dv8tion.jda.api.entities.Member;
 import net.dv8tion.jda.api.entities.MessageChannel;
 import net.dv8tion.jda.api.entities.User;
 
 public class DiscordUser extends AbstractUser {
   private User user;
   private Member member;
   
   public DiscordUser(User discordUser) {
     this.user = discordUser;
   }
   
   public DiscordUser(Member member) {
     this.member = member;
     this.user = member.getUser();
   }
 
   
   public String getName() {
     return this.user.getName();
   }
 
   
   public AbstractMessageChannel openPrivateMessageChannel() {
     return new DiscordMessageChannel((MessageChannel)this.user.openPrivateChannel().complete(), this.user);
   }
 
   
   public boolean hasPermission(String permission) {
     if (this.member == null) {
       return false;
     }
     return this.member.hasPermission(new Permission[] { Permission.valueOf(permission.toUpperCase()) });
   }
 
   
   public boolean ban(String reason) {
     if (this.member == null) {
       return false;
     }
     try {
       this.member.ban(0, reason).complete();
     } catch (Exception e) {
       e.printStackTrace();
       return false;
     } 
     return true;
   }
 
   
   public boolean kick(String reason) {
     if (this.member == null) {
       return false;
     }
     try {
       this.member.kick(reason).complete();
     } catch (Exception e) {
       e.printStackTrace();
       return false;
     } 
     return true;
   }
 }