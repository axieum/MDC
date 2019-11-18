package me.axieum.mcmod.mdc.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.function.Predicate;

public class DiscordUtils
{
    /**
     * Checks permission entries for ALL match on a given Guild member's
     * permissions.
     *
     * @param member      Discord Guild member
     * @param permissions list of permissions in ["user:id",
     *                    "user:username#tag", "user#tag", "role:id"]
     * @return true if the member matches all the given permissions
     */
    public static boolean checkAllPermissions(Member member, List<String> permissions)
    {
        return permissions.stream().allMatch(new PermissionPredicate(member));
    }

    /**
     * Checks permission entries for ANY match on a given Guild member's
     * permissions.
     *
     * @param member      Discord Guild member
     * @param permissions list of permissions in ["user:id",
     *                    "user:username#tag", "user#tag", "role:id"]
     * @return true if the member matches any of the given permissions
     */
    public static boolean checkAnyPermission(Member member, List<String> permissions)
    {
        return permissions.stream().anyMatch(new PermissionPredicate(member));
    }

    /**
     * Discord Permission Predicate for matching a permission string to a
     * member. A permission string must be in the format: "user:id",
     * "user:username#tag", "user#tag" or "role:id".
     */
    private static class PermissionPredicate implements Predicate<String>
    {
        private final Member member;
        private final List<Role> roles;

        public PermissionPredicate(Member member)
        {
            this.member = member;
            this.roles = member.getRoles();
        }

        @Override
        public boolean test(String permission)
        {
            // Check users
            if (permission.startsWith("user:")) {
                String user = permission.substring(5);
                if (user.equals(member.getId()) || user.equals(member.getUser().getAsTag()))
                    return true;
            }

            // Check roles
            if (permission.startsWith("role:")) {
                String role = permission.substring(5);
                if (roles.stream().anyMatch(r -> r.getId().equals(role)))
                    return true;
            }

            // Fallback to matching "user#discriminator"
            return permission.equals(member.getUser().getAsTag());
        }
    }
}
