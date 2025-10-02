package com.evbooking.backend.usecase.service.split;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.model.split.*;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.domain.repository.split.*;
import com.evbooking.backend.presentation.dto.split.*;
import com.evbooking.backend.infrastructure.mapper.split.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PdfExportService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SplitExpenseRepository splitExpenseRepository;
    private final SplitParticipantRepository splitParticipantRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final SplitExpenseMapper splitExpenseMapper;
    private final SplitParticipantMapper splitParticipantMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PdfExportService(GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           SplitExpenseRepository splitExpenseRepository,
                           SplitParticipantRepository splitParticipantRepository,
                           UserRepository userRepository,
                           BalanceService balanceService,
                           GroupMapper groupMapper,
                           GroupMemberMapper groupMemberMapper,
                           SplitExpenseMapper splitExpenseMapper,
                           SplitParticipantMapper splitParticipantMapper) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.splitExpenseRepository = splitExpenseRepository;
        this.splitParticipantRepository = splitParticipantRepository;
        this.userRepository = userRepository;
        this.balanceService = balanceService;
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.splitExpenseMapper = splitExpenseMapper;
        this.splitParticipantMapper = splitParticipantMapper;
    }

    public byte[] generateGroupReport(Long groupId, Long userId) {
        // Verify user has access to this group
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("You are not a member of this group");
        }

        var groupEntityOpt = groupRepository.findById(groupId);
        if (groupEntityOpt.isEmpty()) {
            throw new RuntimeException("Group not found");
        }

        Group group = groupMapper.toDomain(groupEntityOpt.get());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add header
            addGroupReportHeader(document, group);

            // Add group members section
            addGroupMembersSection(document, groupId);

            // Add expenses section
            addGroupExpensesSection(document, groupId, userId);

            // Add balance summary
            addGroupBalanceSummary(document, groupId, userId);

            // Add settlement summary
            addGroupSettlementSummary(document, groupId);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public byte[] generateIndividualReport(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add header
            addIndividualReportHeader(document, user);

            // Add friends balances section
            addFriendsBalancesSection(document, userId);

            // Add groups balances section
            addGroupsBalancesSection(document, userId);

            // Add recent expenses section
            addRecentExpensesSection(document, userId);

            // Add overall summary
            addOverallSummarySection(document, userId);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public byte[] generateFriendReport(Long userId, Long friendId) {
        // Verify users exist
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> friendOpt = userRepository.findById(friendId);
        if (userOpt.isEmpty() || friendOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        User friend = friendOpt.get();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add header
            addFriendReportHeader(document, user, friend);

            // Add shared expenses
            addSharedExpensesSection(document, userId, friendId);

            // Add balance summary
            addFriendBalanceSummary(document, userId, friendId);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addGroupReportHeader(Document document, Group group) {
        // Title
        Paragraph title = new Paragraph("Group Expense Report")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10);
        document.add(title);

        // Group info
        Paragraph groupInfo = new Paragraph()
            .add(new Text("Group: ").setBold())
            .add(group.getName())
            .add("\n")
            .add(new Text("Description: ").setBold())
            .add(group.getDescription() != null ? group.getDescription() : "No description")
            .add("\n")
            .add(new Text("Currency: ").setBold())
            .add(group.getCurrency())
            .add("\n")
            .add(new Text("Generated on: ").setBold())
            .add(LocalDateTime.now().format(DATE_FORMATTER))
            .setMarginBottom(20);
        document.add(groupInfo);
    }

    private void addIndividualReportHeader(Document document, User user) {
        Paragraph title = new Paragraph("Personal Expense Report")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10);
        document.add(title);

        Paragraph userInfo = new Paragraph()
            .add(new Text("User: ").setBold())
            .add(user.getFullName())
            .add(" (@" + user.getUsername() + ")")
            .add("\n")
            .add(new Text("Generated on: ").setBold())
            .add(LocalDateTime.now().format(DATE_FORMATTER))
            .setMarginBottom(20);
        document.add(userInfo);
    }

    private void addFriendReportHeader(Document document, User user, User friend) {
        Paragraph title = new Paragraph("Friend Expense Report")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10);
        document.add(title);

        Paragraph friendInfo = new Paragraph()
            .add(new Text("Between: ").setBold())
            .add(user.getFullName() + " (@" + user.getUsername() + ")")
            .add(" and ")
            .add(friend.getFullName() + " (@" + friend.getUsername() + ")")
            .add("\n")
            .add(new Text("Generated on: ").setBold())
            .add(LocalDateTime.now().format(DATE_FORMATTER))
            .setMarginBottom(20);
        document.add(friendInfo);
    }

    private void addGroupMembersSection(Document document, Long groupId) {
        Paragraph sectionTitle = new Paragraph("Group Members")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        var memberEntities = groupMemberRepository.findByGroupId(groupId);
        java.util.List<GroupMember> members = memberEntities.stream()
            .map(groupMemberMapper::toDomain)
            .collect(java.util.stream.Collectors.toList());

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 1}))
            .setWidth(UnitValue.createPercentValue(100));

        // Headers
        table.addHeaderCell(new Cell().add(new Paragraph("Name").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Username").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Role").setBold()));

        // Data
        for (GroupMember member : members) {
            Optional<User> userOpt = userRepository.findById(member.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                table.addCell(user.getFullName());
                table.addCell("@" + user.getUsername());
                table.addCell(member.isAdmin() ? "Admin" : "Member");
            }
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addGroupExpensesSection(Document document, Long groupId, Long userId) {
        Paragraph sectionTitle = new Paragraph("Group Expenses")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        var expenseEntitiesPage = splitExpenseRepository.findByGroupId(groupId, Pageable.unpaged());
        java.util.List<SplitExpense> expenses = expenseEntitiesPage.getContent().stream()
            .map(splitExpenseMapper::toDomain)
            .collect(java.util.stream.Collectors.toList());

        if (expenses.isEmpty()) {
            document.add(new Paragraph("No expenses found.").setItalic());
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2}))
            .setWidth(UnitValue.createPercentValue(100));

        // Headers
        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Paid By").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Split Type").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));

        // Data
        for (SplitExpense expense : expenses) {
            table.addCell(expense.getDescription());
            table.addCell(expense.getCurrency() + " " + expense.getTotalAmount());

            Optional<User> paidByUser = userRepository.findById(expense.getPaidByUserId());
            table.addCell(paidByUser.map(User::getUsername).orElse("Unknown"));

            table.addCell(expense.getSplitType().name());
            table.addCell(expense.getCreatedAt().format(DATE_FORMATTER));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addGroupBalanceSummary(Document document, Long groupId, Long userId) {
        Paragraph sectionTitle = new Paragraph("Balance Summary")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        java.util.List<GroupBalanceResponse> groupBalances = balanceService.getGroupsBalances(userId, "all");
        Optional<GroupBalanceResponse> currentGroupBalance = groupBalances.stream()
            .filter(balance -> balance.getGroupId().equals(groupId))
            .findFirst();

        if (currentGroupBalance.isPresent()) {
            GroupBalanceResponse balance = currentGroupBalance.get();

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(60));

            table.addCell(new Cell().add(new Paragraph("You Owe").setBold()));
            table.addCell(balance.getCurrency() + " " + balance.getYouOwe());

            table.addCell(new Cell().add(new Paragraph("Owes You").setBold()));
            table.addCell(balance.getCurrency() + " " + balance.getOwesYou());

            table.addCell(new Cell().add(new Paragraph("Net Balance").setBold()));
            String netBalanceText = balance.getCurrency() + " " + balance.getOwesYou().subtract(balance.getYouOwe());
            if (balance.getOwesYou().subtract(balance.getYouOwe()).compareTo(BigDecimal.ZERO) > 0) {
                netBalanceText += " (in your favor)";
            } else if (balance.getOwesYou().subtract(balance.getYouOwe()).compareTo(BigDecimal.ZERO) < 0) {
                netBalanceText += " (you owe)";
            } else {
                netBalanceText += " (settled)";
            }
            table.addCell(netBalanceText);

            document.add(table);
        } else {
            document.add(new Paragraph("No balance data available.").setItalic());
        }

        document.add(new Paragraph("\n"));
    }

    private void addGroupSettlementSummary(Document document, Long groupId) {
        Paragraph sectionTitle = new Paragraph("Settlement Summary")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        // Get all expenses for this group
        var expenseEntitiesPage = splitExpenseRepository.findByGroupId(groupId, Pageable.unpaged());
        java.util.List<SplitExpense> expenses = expenseEntitiesPage.getContent().stream()
            .map(splitExpenseMapper::toDomain)
            .collect(java.util.stream.Collectors.toList());

        Map<Long, BigDecimal> netBalances = new HashMap<>();
        String currency = "INR"; // Default currency

        // Calculate net balances for each member
        for (SplitExpense expense : expenses) {
            currency = expense.getCurrency(); // Use expense currency
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
            java.util.List<SplitParticipant> participants = participantEntities.stream()
                .map(splitParticipantMapper::toDomain)
                .collect(java.util.stream.Collectors.toList());

            for (SplitParticipant participant : participants) {
                if (!participant.isSettled()) {
                    if (participant.getUserId().equals(expense.getPaidByUserId())) {
                        // This person paid, so others owe them
                        BigDecimal othersOwe = participants.stream()
                            .filter(p -> !p.getUserId().equals(expense.getPaidByUserId()) && !p.isSettled())
                            .map(SplitParticipant::getAmountOwed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                        netBalances.merge(participant.getUserId(), othersOwe, BigDecimal::add);
                    } else {
                        // This person owes money
                        netBalances.merge(participant.getUserId(), participant.getAmountOwed().negate(), BigDecimal::add);
                    }
                }
            }
        }

        if (netBalances.isEmpty()) {
            document.add(new Paragraph("All expenses are settled!").setItalic().setFontColor(ColorConstants.GREEN));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1}))
            .setWidth(UnitValue.createPercentValue(80));

        table.addHeaderCell(new Cell().add(new Paragraph("Member").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Net Balance").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()));

        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            Optional<User> userOpt = userRepository.findById(entry.getKey());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                BigDecimal balance = entry.getValue();

                table.addCell(user.getFullName() + " (@" + user.getUsername() + ")");
                table.addCell(currency + " " + balance.abs());

                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    table.addCell(new Cell().add(new Paragraph("Should receive").setFontColor(ColorConstants.GREEN)));
                } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                    table.addCell(new Cell().add(new Paragraph("Should pay").setFontColor(ColorConstants.RED)));
                } else {
                    table.addCell(new Cell().add(new Paragraph("Settled").setFontColor(ColorConstants.BLUE)));
                }
            }
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addFriendsBalancesSection(Document document, Long userId) {
        Paragraph sectionTitle = new Paragraph("Friends Balances")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        java.util.List<FriendBalanceResponse> friendsBalances = balanceService.getFriendsBalances(userId, "all");

        if (friendsBalances.isEmpty()) {
            document.add(new Paragraph("No friend expenses found.").setItalic());
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1}))
            .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("Friend").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("You Owe").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Owes You").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Net Balance").setBold()));

        for (FriendBalanceResponse friend : friendsBalances) {
            table.addCell(friend.getFriendUsername());
            table.addCell(friend.getCurrency() + " " + friend.getYouOwe());
            table.addCell(friend.getCurrency() + " " + friend.getOwesYou());
            table.addCell(friend.getCurrency() + " " + friend.getOwesYou().subtract(friend.getYouOwe()));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addGroupsBalancesSection(Document document, Long userId) {
        Paragraph sectionTitle = new Paragraph("Groups Balances")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        java.util.List<GroupBalanceResponse> groupsBalances = balanceService.getGroupsBalances(userId, "all");

        if (groupsBalances.isEmpty()) {
            document.add(new Paragraph("No group expenses found.").setItalic());
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1}))
            .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("Group").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("You Owe").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Owes You").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Net Balance").setBold()));

        for (GroupBalanceResponse group : groupsBalances) {
            table.addCell(group.getGroupName());
            table.addCell(group.getCurrency() + " " + group.getYouOwe());
            table.addCell(group.getCurrency() + " " + group.getOwesYou());
            table.addCell(group.getCurrency() + " " + group.getOwesYou().subtract(group.getYouOwe()));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addRecentExpensesSection(Document document, Long userId) {
        Paragraph sectionTitle = new Paragraph("Recent Expenses")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        var expenseEntities = splitExpenseRepository.findExpensesByParticipantUserId(userId);
        java.util.List<SplitExpense> recentExpenses = expenseEntities.stream()
            .map(splitExpenseMapper::toDomain)
            .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()))
            .limit(10)
            .collect(Collectors.toList());

        if (recentExpenses.isEmpty()) {
            document.add(new Paragraph("No recent expenses found.").setItalic());
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}))
            .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));

        for (SplitExpense expense : recentExpenses) {
            table.addCell(expense.getDescription());
            table.addCell(expense.getCurrency() + " " + expense.getTotalAmount());
            table.addCell(expense.getGroupId() != null ? "Group" : "Individual");
            table.addCell(expense.getCreatedAt().format(DATE_FORMATTER));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addOverallSummarySection(Document document, Long userId) {
        Paragraph sectionTitle = new Paragraph("Overall Summary")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        OverallBalanceSummaryResponse summary = balanceService.getOverallBalanceSummary(userId);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .setWidth(UnitValue.createPercentValue(60));

        table.addCell(new Cell().add(new Paragraph("Total You Owe").setBold()));
        table.addCell(summary.getPrimaryCurrency() + " " + summary.getTotalYouOwe());

        table.addCell(new Cell().add(new Paragraph("Total Owes You").setBold()));
        table.addCell(summary.getPrimaryCurrency() + " " + summary.getTotalOwesYou());

        table.addCell(new Cell().add(new Paragraph("Net Balance").setBold()));
        String netBalanceText = summary.getPrimaryCurrency() + " " + summary.getNetBalance();
        if (summary.getNetBalance().compareTo(BigDecimal.ZERO) > 0) {
            netBalanceText += " (in your favor)";
        } else if (summary.getNetBalance().compareTo(BigDecimal.ZERO) < 0) {
            netBalanceText += " (you owe)";
        } else {
            netBalanceText += " (settled)";
        }
        table.addCell(netBalanceText);

        table.addCell(new Cell().add(new Paragraph("Total Friends").setBold()));
        table.addCell(String.valueOf(summary.getTotalFriends()));

        table.addCell(new Cell().add(new Paragraph("Total Groups").setBold()));
        table.addCell(String.valueOf(summary.getTotalGroups()));

        table.addCell(new Cell().add(new Paragraph("Total Expenses").setBold()));
        table.addCell(String.valueOf(summary.getTotalExpenses()));

        document.add(table);
    }

    private void addSharedExpensesSection(Document document, Long userId, Long friendId) {
        Paragraph sectionTitle = new Paragraph("Shared Expenses")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        // Get expenses where both users are participants
        var allExpenseEntities = splitExpenseRepository.findExpensesByParticipantUserId(userId);
        java.util.List<SplitExpense> allExpenses = allExpenseEntities.stream()
            .map(splitExpenseMapper::toDomain)
            .collect(java.util.stream.Collectors.toList());
        java.util.List<SplitExpense> sharedExpenses = new java.util.ArrayList<>();

        for (SplitExpense expense : allExpenses) {
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
            java.util.List<SplitParticipant> participants = participantEntities.stream()
                .map(splitParticipantMapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
            boolean bothParticipate = participants.stream()
                .map(SplitParticipant::getUserId)
                .collect(Collectors.toSet())
                .containsAll(Arrays.asList(userId, friendId));

            if (bothParticipate) {
                sharedExpenses.add(expense);
            }
        }

        if (sharedExpenses.isEmpty()) {
            document.add(new Paragraph("No shared expenses found.").setItalic());
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}))
            .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Paid By").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));

        for (SplitExpense expense : sharedExpenses) {
            table.addCell(expense.getDescription());
            table.addCell(expense.getCurrency() + " " + expense.getTotalAmount());

            Optional<User> paidByUser = userRepository.findById(expense.getPaidByUserId());
            table.addCell(paidByUser.map(User::getUsername).orElse("Unknown"));

            table.addCell(expense.getCreatedAt().format(DATE_FORMATTER));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addFriendBalanceSummary(Document document, Long userId, Long friendId) {
        Paragraph sectionTitle = new Paragraph("Balance Summary")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(sectionTitle);

        java.util.List<FriendBalanceResponse> friendsBalances = balanceService.getFriendsBalances(userId, "all");
        Optional<FriendBalanceResponse> friendBalance = friendsBalances.stream()
            .filter(balance -> balance.getFriendUserId().equals(friendId))
            .findFirst();

        if (friendBalance.isPresent()) {
            FriendBalanceResponse balance = friendBalance.get();

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(60));

            table.addCell(new Cell().add(new Paragraph("You Owe").setBold()));
            table.addCell(balance.getCurrency() + " " + balance.getYouOwe());

            table.addCell(new Cell().add(new Paragraph("Friend Owes You").setBold()));
            table.addCell(balance.getCurrency() + " " + balance.getOwesYou());

            table.addCell(new Cell().add(new Paragraph("Net Balance").setBold()));
            String netBalanceText = balance.getCurrency() + " " + balance.getOwesYou().subtract(balance.getYouOwe());
            if (balance.getOwesYou().subtract(balance.getYouOwe()).compareTo(BigDecimal.ZERO) > 0) {
                netBalanceText += " (in your favor)";
            } else if (balance.getOwesYou().subtract(balance.getYouOwe()).compareTo(BigDecimal.ZERO) < 0) {
                netBalanceText += " (you owe)";
            } else {
                netBalanceText += " (settled)";
            }
            table.addCell(netBalanceText);

            document.add(table);
        } else {
            document.add(new Paragraph("No balance data available.").setItalic());
        }
    }
}