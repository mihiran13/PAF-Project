package com.smartcampus.config;

import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.Resource;
import com.smartcampus.model.User;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with sample Resource data on startup if no resources exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DataSeeder(ResourceRepository resourceRepository, UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create or find a system admin user to own the seeded resources
        User adminUser = userRepository.findByEmail("it23864856@my.sliit.lk")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email("it23864856@my.sliit.lk")
                                .name("yositha")
                                .password(passwordEncoder.encode("yositha1234"))
                                .role(UserRole.ADMIN)
                                .isActive(true)
                                .build()
                ));

        // Create technician users for ticket assignment testing
        userRepository.findByEmail("technician@smartcampus.lk")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email("technician@smartcampus.lk")
                                .name("John Support")
                                .password(passwordEncoder.encode("tech1234"))
                                .role(UserRole.TECHNICIAN)
                                .isActive(true)
                                .build()
                ));

        userRepository.findByEmail("maintenance@smartcampus.lk")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email("maintenance@smartcampus.lk")
                                .name("Jane Maintenance")
                                .password(passwordEncoder.encode("tech1234"))
                                .role(UserRole.TECHNICIAN)
                                .isActive(true)
                                .build()
                ));

        if (resourceRepository.count() > 0) {
            log.info("Resources already exist. Skipping data seeding.");
            return;
        }

        List<Resource> resources = new ArrayList<>();

        // ── 1. LECTURE_HALL (8 items) ──
        resources.add(buildResource("Main Lecture Hall A", ResourceType.LECTURE_HALL,
                "Large lecture hall with tiered seating and AV equipment", 250,
                "Block A - Ground Floor", adminUser));
        resources.add(buildResource("Main Lecture Hall B", ResourceType.LECTURE_HALL,
                "Spacious hall with projector and surround sound", 200,
                "Block A - First Floor", adminUser));
        resources.add(buildResource("Lecture Hall C", ResourceType.LECTURE_HALL,
                "Medium-sized lecture hall with whiteboard walls", 120,
                "Block B - Ground Floor", adminUser));
        resources.add(buildResource("Lecture Hall D", ResourceType.LECTURE_HALL,
                "Compact lecture hall ideal for tutorials", 80,
                "Block B - First Floor", adminUser));
        resources.add(buildResource("Lecture Hall E", ResourceType.LECTURE_HALL,
                "Corner hall with natural lighting and smart board", 100,
                "Block C - Ground Floor", adminUser));
        resources.add(buildResource("Seminar Hall F", ResourceType.LECTURE_HALL,
                "Seminar-style hall with round table seating", 60,
                "Block C - Second Floor", adminUser));
        resources.add(buildResource("Lecture Hall G", ResourceType.LECTURE_HALL,
                "Hall with video conferencing capability", 150,
                "Block D - Ground Floor", adminUser));
        resources.add(buildResource("Mini Lecture Hall H", ResourceType.LECTURE_HALL,
                "Small hall for guest lectures and workshops", 40,
                "Block D - First Floor", adminUser));

        // ── 2. LAB (9 items) ──
        resources.add(buildResource("Software Engineering Lab", ResourceType.LAB,
                "High-performance PCs with dual monitors for software development", 45,
                "IT Block - Room L01", adminUser));
        resources.add(buildResource("Network & Security Lab", ResourceType.LAB,
                "Cisco routers, switches, and firewall simulation stations", 30,
                "IT Block - Room L02", adminUser));
        resources.add(buildResource("Data Science Lab", ResourceType.LAB,
                "GPU workstations with TensorFlow and Jupyter pre-installed", 35,
                "IT Block - Room L03", adminUser));
        resources.add(buildResource("Electronics Lab", ResourceType.LAB,
                "Oscilloscopes, multimeters, and circuit prototyping stations", 40,
                "Engineering Block - Room L04", adminUser));
        resources.add(buildResource("Physics Research Lab", ResourceType.LAB,
                "Optics equipment, spectrometers, and precision instruments", 25,
                "Science Block - Room L05", adminUser));
        resources.add(buildResource("Chemistry Lab", ResourceType.LAB,
                "Fume hoods, analytical balances, and wet chemistry stations", 30,
                "Science Block - Room L06", adminUser));
        resources.add(buildResource("Embedded Systems Lab", ResourceType.LAB,
                "Arduino, Raspberry Pi, and FPGA development boards", 35,
                "IT Block - Room L07", adminUser));
        resources.add(buildResource("Multimedia Lab", ResourceType.LAB,
                "iMacs with Adobe Creative Suite for design projects", 25,
                "IT Block - Room L08", adminUser));
        resources.add(buildResource("Robotics Lab", ResourceType.LAB,
                "Robot arms, 3D printers, and prototyping workspace", 20,
                "Engineering Block - Room L09", adminUser));

        // ── 3. MEETING_ROOM (7 items) ──
        resources.add(buildResource("Board Room", ResourceType.MEETING_ROOM,
                "Executive boardroom with 65\" display and video conferencing", 16,
                "Admin Block - 3rd Floor", adminUser));
        resources.add(buildResource("Conference Room Alpha", ResourceType.MEETING_ROOM,
                "Professional meeting room with whiteboard and projector", 12,
                "Admin Block - 2nd Floor", adminUser));
        resources.add(buildResource("Conference Room Beta", ResourceType.MEETING_ROOM,
                "Compact meeting room for small team discussions", 8,
                "Admin Block - 2nd Floor", adminUser));
        resources.add(buildResource("Faculty Meeting Room", ResourceType.MEETING_ROOM,
                "Faculty-reserved room with round table and tea station", 20,
                "Faculty Block - 1st Floor", adminUser));
        resources.add(buildResource("Student Council Room", ResourceType.MEETING_ROOM,
                "Student organization meeting space with presentation setup", 15,
                "Student Center - 2nd Floor", adminUser));
        resources.add(buildResource("Innovation Hub Room", ResourceType.MEETING_ROOM,
                "Creative brainstorming space with writable walls", 10,
                "IT Block - 3rd Floor", adminUser));
        resources.add(buildResource("Interview Room", ResourceType.MEETING_ROOM,
                "Quiet room for interviews and one-on-one meetings", 4,
                "Admin Block - 1st Floor", adminUser));

        // ── 4. PROJECTOR (6 items) ──
        resources.add(buildResource("Epson EB-2250U", ResourceType.PROJECTOR,
                "5000 lumens WUXGA projector for large halls", null,
                "AV Storage - Room S01", adminUser));
        resources.add(buildResource("BenQ MW535A", ResourceType.PROJECTOR,
                "Portable WXGA projector for classrooms", null,
                "AV Storage - Room S01", adminUser));
        resources.add(buildResource("Sony VPL-PHZ51", ResourceType.PROJECTOR,
                "Laser projector with 5000 lumens for auditoriums", null,
                "AV Storage - Room S02", adminUser));
        resources.add(buildResource("ViewSonic PX701-4K", ResourceType.PROJECTOR,
                "4K UHD projector for presentations and movie screenings", null,
                "AV Storage - Room S02", adminUser));
        resources.add(buildResource("Epson EB-L200SW", ResourceType.PROJECTOR,
                "Short-throw laser projector for small rooms", null,
                "AV Storage - Room S03", adminUser));
        resources.add(buildResource("LG ProBeam BU50NST", ResourceType.PROJECTOR,
                "4K UHD laser projector for premium presentations", null,
                "AV Storage - Room S03", adminUser));

        // ── 5. CAMERA (5 items) ──
        resources.add(buildResource("Canon EOS R6 Mark II", ResourceType.CAMERA,
                "Full-frame mirrorless camera for events and photography", null,
                "Media Room - Cabinet C1", adminUser));
        resources.add(buildResource("Sony A7 IV", ResourceType.CAMERA,
                "Hybrid photo/video camera for campus media projects", null,
                "Media Room - Cabinet C1", adminUser));
        resources.add(buildResource("Canon XA60", ResourceType.CAMERA,
                "Professional camcorder for lecture recording", null,
                "Media Room - Cabinet C2", adminUser));
        resources.add(buildResource("GoPro HERO12 Black", ResourceType.CAMERA,
                "Action camera for outdoor events and sports coverage", null,
                "Media Room - Cabinet C2", adminUser));
        resources.add(buildResource("Logitech Rally Camera", ResourceType.CAMERA,
                "PTZ conference camera for video conferencing rooms", null,
                "Media Room - Cabinet C3", adminUser));

        // ── 6. OTHER_EQUIPMENT (6 items) ──
        resources.add(buildResource("Wireless Microphone Set", ResourceType.OTHER_EQUIPMENT,
                "Shure dual-channel wireless microphone system", null,
                "AV Storage - Room S01", adminUser));
        resources.add(buildResource("Portable PA System", ResourceType.OTHER_EQUIPMENT,
                "JBL EON715 powered speaker for outdoor events", null,
                "AV Storage - Room S01", adminUser));
        resources.add(buildResource("Document Scanner Station", ResourceType.OTHER_EQUIPMENT,
                "Fujitsu high-speed document scanner for admin use", null,
                "Admin Block - 1st Floor", adminUser));
        resources.add(buildResource("3D Printer - Prusa MK4", ResourceType.OTHER_EQUIPMENT,
                "FDM 3D printer for prototyping and student projects", null,
                "Engineering Block - Room L09", adminUser));
        resources.add(buildResource("Laser Cutter Machine", ResourceType.OTHER_EQUIPMENT,
                "CO2 laser cutter for precision fabrication", null,
                "Engineering Block - Workshop", adminUser));
        resources.add(buildResource("Portable Whiteboard Set", ResourceType.OTHER_EQUIPMENT,
                "Rolling double-sided whiteboards with marker kits", null,
                "Student Center - Storage", adminUser));

        // ── 7. LIBRARY (7 items) ──
        resources.add(buildResource("Main Library Hall", ResourceType.LIBRARY,
                "Central library with 3 floors of books and periodicals", 300,
                "Library Building - All Floors", adminUser));
        resources.add(buildResource("Digital Library Lab", ResourceType.LIBRARY,
                "Computer terminals with access to IEEE, ACM, and Springer", 40,
                "Library Building - 2nd Floor", adminUser));
        resources.add(buildResource("Reference Section", ResourceType.LIBRARY,
                "Non-circulating reference books, encyclopedias, and journals", 50,
                "Library Building - Ground Floor", adminUser));
        resources.add(buildResource("Group Study Pod A", ResourceType.LIBRARY,
                "Sound-insulated pod for group research and discussions", 6,
                "Library Building - 1st Floor", adminUser));
        resources.add(buildResource("Group Study Pod B", ResourceType.LIBRARY,
                "Glass-walled study pod with display screen", 6,
                "Library Building - 1st Floor", adminUser));
        resources.add(buildResource("Silent Reading Zone", ResourceType.LIBRARY,
                "Individual carrels in a noise-free reading environment", 80,
                "Library Building - 3rd Floor", adminUser));
        resources.add(buildResource("Archive Room", ResourceType.LIBRARY,
                "Historical theses, rare publications, and special collections", 15,
                "Library Building - Basement", adminUser));

        // ── 8. AUDITORIUM (5 items) ──
        resources.add(buildResource("Grand Auditorium", ResourceType.AUDITORIUM,
                "Main campus auditorium with professional stage and lighting", 500,
                "Auditorium Complex - Main Hall", adminUser));
        resources.add(buildResource("Mini Auditorium", ResourceType.AUDITORIUM,
                "Smaller auditorium for departmental events and seminars", 150,
                "Auditorium Complex - Side Hall", adminUser));
        resources.add(buildResource("Open Air Theater", ResourceType.AUDITORIUM,
                "Outdoor amphitheater for cultural events and performances", 350,
                "Campus Grounds - East Wing", adminUser));
        resources.add(buildResource("Cinema Hall", ResourceType.AUDITORIUM,
                "Screening room with Dolby audio for film and media events", 100,
                "Student Center - Ground Floor", adminUser));
        resources.add(buildResource("Convocation Hall", ResourceType.AUDITORIUM,
                "Formal hall for graduation ceremonies and award functions", 600,
                "Administration Complex - Main Wing", adminUser));

        // ── 9. SPORTS_FACILITY (8 items) ──
        resources.add(buildResource("Indoor Basketball Court", ResourceType.SPORTS_FACILITY,
                "Full-size indoor basketball court with LED scoreboard", 30,
                "Sports Complex - Indoor Arena", adminUser));
        resources.add(buildResource("Swimming Pool", ResourceType.SPORTS_FACILITY,
                "Olympic-size 50m swimming pool with changing rooms", 40,
                "Sports Complex - Aquatic Center", adminUser));
        resources.add(buildResource("Gymnasium", ResourceType.SPORTS_FACILITY,
                "Fully equipped gym with cardio and weight stations", 50,
                "Sports Complex - Fitness Block", adminUser));
        resources.add(buildResource("Cricket Practice Nets", ResourceType.SPORTS_FACILITY,
                "4-lane practice nets with bowling machine", 20,
                "Sports Ground - North End", adminUser));
        resources.add(buildResource("Tennis Court A", ResourceType.SPORTS_FACILITY,
                "Hard court tennis court with floodlights", 4,
                "Sports Complex - Outdoor Courts", adminUser));
        resources.add(buildResource("Football Field", ResourceType.SPORTS_FACILITY,
                "FIFA-standard grass pitch with spectator stands", 100,
                "Sports Ground - Main Field", adminUser));
        resources.add(buildResource("Badminton Hall", ResourceType.SPORTS_FACILITY,
                "4-court badminton hall with wooden flooring", 24,
                "Sports Complex - Indoor Arena", adminUser));
        resources.add(buildResource("Table Tennis Room", ResourceType.SPORTS_FACILITY,
                "Room with 6 professional-grade table tennis tables", 12,
                "Student Center - 1st Floor", adminUser));

        // ── 10. STUDY_ROOM (6 items) ──
        resources.add(buildResource("Quiet Study Room 1", ResourceType.STUDY_ROOM,
                "Individual study room with desk lamp and power outlets", 1,
                "Library Building - 2nd Floor", adminUser));
        resources.add(buildResource("Quiet Study Room 2", ResourceType.STUDY_ROOM,
                "Private study room with adjustable desk and chair", 1,
                "Library Building - 2nd Floor", adminUser));
        resources.add(buildResource("Collaborative Study Room A", ResourceType.STUDY_ROOM,
                "Group study room with whiteboard and wall-mounted display", 8,
                "Student Center - 2nd Floor", adminUser));
        resources.add(buildResource("Collaborative Study Room B", ResourceType.STUDY_ROOM,
                "Team study space with modular furniture", 8,
                "Student Center - 2nd Floor", adminUser));
        resources.add(buildResource("Postgraduate Study Room", ResourceType.STUDY_ROOM,
                "Dedicated study room for postgraduate researchers", 15,
                "Block C - 3rd Floor", adminUser));
        resources.add(buildResource("24/7 Study Lounge", ResourceType.STUDY_ROOM,
                "Always-open study area with vending machines and WiFi", 30,
                "Student Center - Ground Floor", adminUser));

        resourceRepository.saveAll(resources);
        log.info("✅ Seeded {} resources across 10 resource types into the database.", resources.size());
    }

    private Resource buildResource(String name, ResourceType type, String description,
                                    Integer capacity, String location, User createdBy) {
        return Resource.builder()
                .name(name)
                .type(type)
                .description(description)
                .capacity(capacity)
                .location(location)
                .status(ResourceStatus.ACTIVE)
                .createdBy(createdBy)
                .build();
    }
}
