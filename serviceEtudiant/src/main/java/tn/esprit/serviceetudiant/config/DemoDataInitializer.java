package tn.esprit.serviceetudiant.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tn.esprit.serviceetudiant.entity.ChatConversation;
import tn.esprit.serviceetudiant.entity.ChatMessage;
import tn.esprit.serviceetudiant.entity.ServiceRequest;
import tn.esprit.serviceetudiant.entity.StudentService;
import tn.esprit.serviceetudiant.enums.DeliveryMode;
import tn.esprit.serviceetudiant.enums.ServiceCategory;
import tn.esprit.serviceetudiant.enums.ServiceModerationStatus;
import tn.esprit.serviceetudiant.enums.ServiceRequestStatus;
import tn.esprit.serviceetudiant.repository.ChatConversationRepository;
import tn.esprit.serviceetudiant.repository.ChatMessageRepository;
import tn.esprit.serviceetudiant.repository.ServiceRequestRepository;
import tn.esprit.serviceetudiant.repository.StudentServiceRepository;
import tn.esprit.serviceetudiant.service.StudentServiceService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final StudentServiceRepository studentServiceRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final StudentServiceService studentServiceService;

    @Override
    public void run(String... args) {
        if (studentServiceRepository.count() > 0) {
            return;
        }

        StudentService pythonSupport = studentServiceRepository.save(StudentService.builder()
                .title("Python Debugging and Mini Project Rescue")
                .slug("python-debugging-mini-project-rescue")
                .shortDescription("Fast help for broken scripts, API calls, and class mini-project blockers.")
                .category(ServiceCategory.TECH)
                .priceLabel("20 TND / hour")
                .priceValue(new BigDecimal("20.00"))
                .deliveryMode(DeliveryMode.ONLINE)
                .tags(List.of("python", "debugging", "api", "mini-project"))
                .location("Discord / Google Meet")
                .providerId(101L)
                .providerName("Med Ali")
                .providerHeadline("Software engineering student focused on clean practical fixes.")
                .providerAvatar("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=256&q=80")
                .providerDepartment("Software Engineering")
                .coverImageUrl("https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=1200&q=80")
                .featured(true)
                .requestCount(0)
                .createdAt(Instant.parse("2026-03-08T12:00:00Z"))
                .updatedAt(Instant.parse("2026-03-26T08:45:00Z"))
                .build());

        StudentService presentationPolish = studentServiceRepository.save(StudentService.builder()
                .title("UI/UX Project Presentation Polish")
                .slug("ui-ux-project-presentation-polish")
                .shortDescription("I help you clean slides, fix hierarchy, and present with more confidence.")
                .category(ServiceCategory.CREATIVE)
                .priceLabel("35 TND / pack")
                .priceValue(new BigDecimal("35.00"))
                .deliveryMode(DeliveryMode.HYBRID)
                .tags(List.of("slides", "pitching", "figma", "storytelling"))
                .location("ESPRIT Ariana / Online")
                .providerId(201L)
                .providerName("Sarra Ben Salem")
                .providerHeadline("Product design student helping teams present sharper work.")
                .providerAvatar("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80")
                .providerDepartment("Business Computing")
                .coverImageUrl("https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80")
                .featured(true)
                .requestCount(0)
                .createdAt(Instant.parse("2026-03-12T09:10:00Z"))
                .updatedAt(Instant.parse("2026-03-25T18:15:00Z"))
                .build());

        StudentService errands = studentServiceRepository.save(StudentService.builder()
                .title("Campus Errands and Quick Document Pickup")
                .slug("campus-errands-quick-document-pickup")
                .shortDescription("Need a paper dropped off, printed, or picked up on campus? I can help.")
                .category(ServiceCategory.ERRANDS)
                .priceLabel("From 10 TND")
                .priceValue(new BigDecimal("10.00"))
                .deliveryMode(DeliveryMode.ON_SITE)
                .tags(List.of("documents", "printing", "campus", "pickup"))
                .location("Ariana / Campus area")
                .providerId(301L)
                .providerName("Youssef Gharbi")
                .providerHeadline("Reliable for quick practical tasks around campus.")
                .providerAvatar("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=256&q=80")
                .providerDepartment("Networks & Security")
                .coverImageUrl("https://images.unsplash.com/photo-1521791136064-7986c2920216?auto=format&fit=crop&w=1200&q=80")
                .featured(false)
                .requestCount(0)
                .createdAt(Instant.parse("2026-03-10T14:30:00Z"))
                .updatedAt(Instant.parse("2026-03-23T10:20:00Z"))
                .build());

        StudentService algorithms = studentServiceRepository.save(StudentService.builder()
                .title("Algorithms Revision Sprint")
                .slug("algorithms-revision-sprint")
                .shortDescription("Condensed revision sessions for graphs, DP, sorting, and complexity.")
                .category(ServiceCategory.ACADEMIC)
                .priceLabel("25 TND / session")
                .priceValue(new BigDecimal("25.00"))
                .deliveryMode(DeliveryMode.HYBRID)
                .tags(List.of("algorithms", "revision", "complexity", "exam prep"))
                .location("Library / Online")
                .providerId(111L)
                .providerName("Leila Mansour")
                .providerHeadline("Structured academic support with practical exercises.")
                .providerAvatar("https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=256&q=80")
                .providerDepartment("Data Science")
                .coverImageUrl("https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80")
                .featured(true)
                .requestCount(0)
                .createdAt(Instant.parse("2026-03-05T07:45:00Z"))
                .updatedAt(Instant.parse("2026-03-24T19:00:00Z"))
                .build());

        ServiceRequest acceptedRequest = serviceRequestRepository.save(ServiceRequest.builder()
                .serviceId(presentationPolish.getId())
                .serviceTitle(presentationPolish.getTitle())
                .serviceCategory(presentationPolish.getCategory())
                .requesterId(702L)
                .requesterName("Moez Ayadi")
                .requesterDepartment("Business Computing")
                .requesterAvatar("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=200&q=80")
                .providerId(presentationPolish.getProviderId())
                .providerName(presentationPolish.getProviderName())
                .message("Need help reorganizing a 10-slide jury deck before Friday.")
                .preferredDate("Friday at 16:00")
                .status(ServiceRequestStatus.ACCEPTED)
                .budgetLabel("Pack agreed")
                .createdAt(Instant.parse("2026-03-25T13:15:00Z"))
                .build());

        ServiceRequest pendingRequest = serviceRequestRepository.save(ServiceRequest.builder()
                .serviceId(pythonSupport.getId())
                .serviceTitle(pythonSupport.getTitle())
                .serviceCategory(pythonSupport.getCategory())
                .requesterId(701L)
                .requesterName("Hela Trabelsi")
                .requesterDepartment("Software Engineering")
                .requesterAvatar("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=200&q=80")
                .providerId(pythonSupport.getProviderId())
                .providerName(pythonSupport.getProviderName())
                .message("I am blocked on JWT refresh flow and would like a quick review tonight.")
                .preferredDate("Tonight after 20:00")
                .status(ServiceRequestStatus.PENDING)
                .budgetLabel("Budget 25 TND")
                .createdAt(Instant.parse("2026-03-26T19:30:00Z"))
                .build());

        ServiceRequest completedRequest = serviceRequestRepository.save(ServiceRequest.builder()
                .serviceId(errands.getId())
                .serviceTitle(errands.getTitle())
                .serviceCategory(errands.getCategory())
                .requesterId(101L)
                .requesterName("Med Ali")
                .requesterDepartment("Software Engineering")
                .requesterAvatar("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80")
                .providerId(errands.getProviderId())
                .providerName(errands.getProviderName())
                .message("Can you pick up a printed copy from the campus print shop tomorrow?")
                .preferredDate("Tomorrow morning")
                .status(ServiceRequestStatus.COMPLETED)
                .budgetLabel("12 TND")
                .createdAt(Instant.parse("2026-03-21T08:10:00Z"))
                .build());

        ChatConversation acceptedConversation = chatConversationRepository.save(ChatConversation.builder()
                .requestId(acceptedRequest.getId())
                .serviceId(acceptedRequest.getServiceId())
                .serviceTitle(acceptedRequest.getServiceTitle())
                .requesterId(acceptedRequest.getRequesterId())
                .requesterName(acceptedRequest.getRequesterName())
                .providerId(acceptedRequest.getProviderId())
                .providerName(acceptedRequest.getProviderName())
                .active(true)
                .createdAt(Instant.parse("2026-03-25T13:20:00Z"))
                .updatedAt(Instant.parse("2026-03-25T13:40:00Z"))
                .build());

        chatMessageRepository.saveAll(List.of(
                ChatMessage.builder()
                        .conversationId(acceptedConversation.getId())
                        .senderId(acceptedRequest.getRequesterId())
                        .senderName(acceptedRequest.getRequesterName())
                        .content("Hi, I sent the latest deck version. Can we make it sharper before Friday?")
                        .sentAt(Instant.parse("2026-03-25T13:22:00Z"))
                        .build(),
                ChatMessage.builder()
                        .conversationId(acceptedConversation.getId())
                        .senderId(acceptedRequest.getProviderId())
                        .senderName(acceptedRequest.getProviderName())
                        .content("Yes, absolutely. I will reorganize the first slides and tighten the hierarchy.")
                        .sentAt(Instant.parse("2026-03-25T13:40:00Z"))
                        .build()
        ));

        ChatConversation completedConversation = chatConversationRepository.save(ChatConversation.builder()
                .requestId(completedRequest.getId())
                .serviceId(completedRequest.getServiceId())
                .serviceTitle(completedRequest.getServiceTitle())
                .requesterId(completedRequest.getRequesterId())
                .requesterName(completedRequest.getRequesterName())
                .providerId(completedRequest.getProviderId())
                .providerName(completedRequest.getProviderName())
                .active(true)
                .createdAt(Instant.parse("2026-03-21T08:20:00Z"))
                .updatedAt(Instant.parse("2026-03-21T09:15:00Z"))
                .build());

        chatMessageRepository.saveAll(List.of(
                ChatMessage.builder()
                        .conversationId(completedConversation.getId())
                        .senderId(completedRequest.getRequesterId())
                        .senderName(completedRequest.getRequesterName())
                        .content("Can you pick up a printed copy from the campus print shop tomorrow?")
                        .sentAt(Instant.parse("2026-03-21T08:21:00Z"))
                        .build(),
                ChatMessage.builder()
                        .conversationId(completedConversation.getId())
                        .senderId(completedRequest.getProviderId())
                        .senderName(completedRequest.getProviderName())
                        .content("Done. I picked it up and left it at the usual campus meeting point.")
                        .sentAt(Instant.parse("2026-03-21T09:15:00Z"))
                        .build()
        ));

        studentServiceService.moderateService(pythonSupport.getId(), ServiceModerationStatus.APPROVED);
        studentServiceService.moderateService(presentationPolish.getId(), ServiceModerationStatus.APPROVED);
        studentServiceService.moderateService(errands.getId(), ServiceModerationStatus.APPROVED);
        studentServiceService.refreshRequestMetrics(pythonSupport.getId());
        studentServiceService.refreshRequestMetrics(presentationPolish.getId());
        studentServiceService.refreshRequestMetrics(errands.getId());
    }
}


