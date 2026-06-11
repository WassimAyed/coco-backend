package tn.esprit.lostfoundservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.esprit.lostfoundservice.entity.LostItem;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.LostItemType;
import tn.esprit.lostfoundservice.repository.LostItemRepository;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class LostItemDataSeeder {

    private final LostItemRepository lostItemRepository;

    @Bean
    public CommandLineRunner seedLostItems() {
        return args -> {
            if (lostItemRepository.count() > 0) {
                return;
            }

            List<LostItem> demoItems = List.of(
                LostItem.builder()
                    .title("Samsung charger lost in Library")
                    .description("Black Samsung charger with a USB-C cable, last seen near the main library desks.")
                    .type(LostItemType.LOST)
                    .category("Electronics")
                    .location("Library")
                    .dateTime(LocalDateTime.now().minusDays(2))
                    .contactInfo("Contact the student office")
                    .status(LostItemStatus.ACTIVE)
                    .userId(1001L)
                    .imageUrl("https://images.unsplash.com/photo-1583863788434-e58a36330f84?w=900")
                    .build(),
                LostItem.builder()
                    .title("Found student ID card")
                    .description("Student card found near the cafeteria entrance.")
                    .type(LostItemType.FOUND)
                    .category("Documents")
                    .location("Cafeteria")
                    .dateTime(LocalDateTime.now().minusDays(1).minusHours(3))
                    .contactInfo("Available at reception")
                    .status(LostItemStatus.ACTIVE)
                    .userId(1002L)
                    .imageUrl("https://images.unsplash.com/photo-1576669801996-5f053f87e5f5?w=900")
                    .build(),
                LostItem.builder()
                    .title("Lost house keys")
                    .description("A small keyring with two silver keys and a red strap.")
                    .type(LostItemType.LOST)
                    .category("Keys")
                    .location("Parking")
                    .dateTime(LocalDateTime.now().minusDays(4))
                    .contactInfo("Reach the owner via admin desk")
                    .status(LostItemStatus.ACTIVE)
                    .userId(1001L)
                    .imageUrl("https://images.unsplash.com/photo-1513708929626-cc3303f84d30?w=900")
                    .build(),
                LostItem.builder()
                    .title("Found headphones")
                    .description("Wireless headphones found in the campus hall.")
                    .type(LostItemType.FOUND)
                    .category("Accessories")
                    .location("Campus A")
                    .dateTime(LocalDateTime.now().minusHours(18))
                    .contactInfo("Ask security at desk 2")
                    .status(LostItemStatus.ACTIVE)
                    .userId(1002L)
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=900")
                    .build(),
                LostItem.builder()
                    .title("Resolved lost notebook")
                    .description("Notebook was returned to its owner after a claim.")
                    .type(LostItemType.LOST)
                    .category("Documents")
                    .location("Campus B")
                    .dateTime(LocalDateTime.now().minusDays(6))
                    .contactInfo("Closed")
                    .status(LostItemStatus.RESOLVED)
                    .userId(1001L)
                    .imageUrl("https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=900")
                    .build(),
                LostItem.builder()
                    .title("Found glasses")
                    .description("Pair of glasses found in the lecture hall.")
                    .type(LostItemType.FOUND)
                    .category("Accessories")
                    .location("Lecture Hall")
                    .dateTime(LocalDateTime.now().minusDays(3).minusHours(5))
                    .contactInfo("Lost & Found desk")
                    .status(LostItemStatus.ACTIVE)
                    .userId(1002L)
                    .imageUrl("https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=900")
                    .build()
            );

            lostItemRepository.saveAll(demoItems);
        };
    }
}