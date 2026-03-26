package com.durumi.bridge.command;

import com.durumi.bridge.DurumiBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DurumiCommand implements CommandExecutor, TabCompleter {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DurumiBridge plugin;

    public DurumiCommand(DurumiBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "announce" -> handleAnnounce(sender, args);
            case "verify" -> handleVerify(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("durumi.admin")) {
            sender.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return;
        }

        plugin.reloadPlugin();
        sender.sendMessage(Component.text("[DurumiBridge] ", NamedTextColor.GOLD)
                .append(Component.text("설정이 리로드되었습니다!", NamedTextColor.GREEN)));
    }

    private void handleAnnounce(CommandSender sender, String[] args) {
        if (!sender.hasPermission("durumi.admin")) {
            sender.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("[DurumiBridge] ", NamedTextColor.GOLD)
                    .append(Component.text("사용법: /durumi announce <제목> | <내용>", NamedTextColor.YELLOW)));
            return;
        }

        String fullText = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String title;
        String content;

        int separatorIndex = fullText.indexOf('|');
        if (separatorIndex >= 0) {
            title = fullText.substring(0, separatorIndex).trim();
            content = fullText.substring(separatorIndex + 1).trim();
        } else {
            title = fullText;
            content = fullText;
        }

        if (title.isEmpty() || content.isEmpty()) {
            sender.sendMessage(Component.text("[DurumiBridge] ", NamedTextColor.GOLD)
                    .append(Component.text("제목과 내용을 모두 입력해주세요.", NamedTextColor.RED)));
            return;
        }

        String author = sender.getName();

        int id = plugin.getDatabaseManager().createAnnouncement(title, content, author, "일반", false);
        if (id > 0) {
            sender.sendMessage(Component.text("[DurumiBridge] ", NamedTextColor.GOLD)
                    .append(Component.text("공지사항이 등록되었습니다! (ID: " + id + ")", NamedTextColor.GREEN)));
        } else {
            sender.sendMessage(Component.text("[DurumiBridge] ", NamedTextColor.GOLD)
                    .append(Component.text("공지사항 등록에 실패했습니다.", NamedTextColor.RED)));
        }
    }

    private void handleVerify(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("이 명령어는 플레이어만 사용할 수 있습니다.", NamedTextColor.RED));
            return;
        }

        if (!player.hasPermission("durumi.verify")) {
            player.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return;
        }

        // Generate a random verification code
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        String codeStr = code.toString();
        String result = plugin.getDatabaseManager().createVerificationCode(
                player.getName(), player.getUniqueId().toString(), codeStr);

        if (result != null) {
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("═══ ", NamedTextColor.GOLD)
                    .append(Component.text("두루미마을 계정 인증", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(" ═══", NamedTextColor.GOLD)));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("  인증 코드: ", NamedTextColor.GRAY)
                    .append(Component.text(codeStr, NamedTextColor.AQUA, TextDecoration.BOLD)));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("  웹사이트에서 이 코드를 입력하여", NamedTextColor.GRAY));
            player.sendMessage(Component.text("  마인크래프트 계정을 연동하세요.", NamedTextColor.GRAY));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("  ⚠ 이 코드는 1회 사용 가능합니다.", NamedTextColor.YELLOW));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        } else {
            player.sendMessage(Component.text("[DurumiBridge] ", NamedTextColor.GOLD)
                    .append(Component.text("인증 코드 생성에 실패했습니다. 다시 시도해주세요.", NamedTextColor.RED)));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("═══ ", NamedTextColor.GOLD)
                .append(Component.text("DurumiBridge 도움말", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(" ═══", NamedTextColor.GOLD)));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  /durumi reload", NamedTextColor.AQUA)
                .append(Component.text(" - 설정 리로드", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /durumi announce <제목> | <내용>", NamedTextColor.AQUA)
                .append(Component.text(" - 공지사항 등록", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /durumi verify", NamedTextColor.AQUA)
                .append(Component.text(" - 계정 인증 코드 생성", NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String sub : List.of("reload", "announce", "verify")) {
                if (sub.startsWith(partial)) {
                    completions.add(sub);
                }
            }
        }

        return completions;
    }
}
