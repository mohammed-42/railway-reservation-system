package com.example.railwayreservation.controller;

import com.example.railwayreservation.entity.Booking;
import com.example.railwayreservation.repository.BookingRepository;
import com.example.railwayreservation.service.EmailService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Controller
public class BookingController {
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Autowired
    public BookingController(BookingRepository bookingRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("booking", new Booking());
        return "index";
    }

    @GetMapping("/api/trains")
    @ResponseBody
    public List<Map<String, String>> trains(@RequestParam String source, @RequestParam String destination) {
        String key = source.trim().toLowerCase() + "-" + destination.trim().toLowerCase();
        Map<String, List<String>> routeTrains = buildTrainMap();
        List<String> names = routeTrains.getOrDefault(
                key,
                routeTrains.getOrDefault(destination.trim().toLowerCase() + "-" + source.trim().toLowerCase(), List.of())
        );

        if (names.isEmpty()) {
            // fallback small list
            names = List.of("Superfast Express", "Jan Shatabdi", "Passenger Special", "Intercity Express");
        }

        List<Map<String, String>> out = new ArrayList<>();
        for (String n : names) {
            Map<String, String> m = new HashMap<>();
            m.put("name", n);
            m.put("source", source);
            m.put("destination", destination);
            out.add(m);
        }
        return out;
    }

    private Map<String, List<String>> buildTrainMap() {
        Map<String, List<String>> m = new HashMap<>();
        // Delhi - Mumbai
        m.put("delhi-mumbai", List.of("Rajdhani Express", "August Kranti Rajdhani", "Mumbai Rajdhani",
                "Garib Rath", "Duronto Express", "Mumbai Mail"));
        // Delhi - Bangalore
        m.put("delhi-bangalore", List.of("Karnataka Express", "Yeshvantpur Express", "Bengaluru Rajdhani",
                "Gatimaan Express", "Chalukya Express"));
        // Delhi - Chennai
        m.put("delhi-chennai", List.of("Grand Trunk Express", "Tamil Nadu Express", "Dakshin Express",
                "Rajdhani Express (Chennai)"));
        // Delhi - Kolkata
        m.put("delhi-kolkata", List.of("Poorva Express", "Howrah Mail", "Rajdhani Express (Kolkata)", "Nizamuddin Express"));
        // Mumbai - Bangalore
        m.put("mumbai-bangalore", List.of("Udyan Express", "Duronto Express", "Sampoorna Kranti Express",
                "Kaveri Express", "Mahalaxmi Express"));
        // Mumbai - Chennai
        m.put("mumbai-chennai", List.of("Chennai Mail", "Kanyakumari Express", "Thiruvananthapuram Express",
                "Mumbai Chennai Express"));
        // Mumbai - Kolkata
        m.put("mumbai-kolkata", List.of("Lokmanya Tilak Express", "Howrah Mail", "Gitanjali Express", "Swarna Jayanti Express"));
        // Bangalore - Chennai
        m.put("bangalore-chennai", List.of("Shatabdi Express", "Cheran Express", "Brindavan Express", "Tuticorin Express"));
        // Bangalore - Kolkata
        m.put("bangalore-kolkata", List.of("Swarna Jayanti Express", "Bengaluru Kolkata Express", "Darjeeling Mail"));
        // Chennai - Kolkata
        m.put("chennai-kolkata", List.of("Coromandel Express", "Howrah Express", "Kolkata Express", "Ernakulam Express"));
        // Delhi - Hyderabad
        m.put("delhi-hyderabad", List.of("Nizamuddin Express", "Telangana Express", "Scindia Express", "Hyderabad Rajdhani"));
        // Mumbai - Pune
        m.put("mumbai-pune", List.of("Deccan Express", "Sinhagad Express", "Konkan Kanya Express", "Intercity Express"));
        // Delhi - Jaipur
        m.put("delhi-jaipur", List.of("Shatabdi Express", "Ajmer Shatabdi", "Intercity Express", "Mewar Express"));
        // Mumbai - Goa (Madgaon)
        m.put("mumbai-goa", List.of("Konkan Kanya", "Mandovi Express", "Jan Shatabdi", "Matsyagandha Express"));
        // Delhi - Pune
        m.put("delhi-pune", List.of("Pune Duronto", "Pune Express", "Humsafar Express", "Deccan Queen"));
        // Kolkata - Hyderabad
        m.put("kolkata-hyderabad", List.of("Falaknuma Express", "Kolkata Mail", "Godavari Express"));
        // Chennai - Hyderabad
        m.put("chennai-hyderabad", List.of("Krishna Express", "Chennai Mail", "Charminar Express"));
        // Bangalore - Pune
        m.put("bangalore-pune", List.of("Koyna Express", "Sahyadri Express", "Bengaluru Express"));
        // Jaipur - Mumbai
        m.put("jaipur-mumbai", List.of("Ajmer Express", "Jaipur Express", "Rail King"));
        // Pune - Delhi
        m.put("pune-delhi", List.of("Pune Rajdhani", "Pune Superfast", "Sampark Kranti"));
        // Hyderabad - Mumbai
        m.put("hyderabad-mumbai", List.of("Hyderabad Express", "Godavari Express", "Mumbai Mail"));
        // Kochi - Mumbai
        m.put("kochi-mumbai", List.of("Mavali Express", "Ernad Express", "Netravati Express"));
        return m;
    }

    @PostMapping("/book")
    public String book(Booking booking, Model model) {
        Random r = new Random();
        if (booking.getCoach() == null || booking.getCoach().isBlank()) {
            String[] coaches = {"A1", "A2", "B1", "B2", "S1", "S2"};
            booking.setCoach(coaches[r.nextInt(coaches.length)]);
        }
        if (booking.getSeat() == null || booking.getSeat().isBlank()) {
            int seatNo = r.nextInt(72) + 1;
            booking.setSeat(String.valueOf(seatNo));
        }
        Booking saved = bookingRepository.save(booking);

        String body = "Hello " + saved.getName() + ",\n\n" +
                "Your ticket is confirmed!\n" +
                "Train: " + saved.getTrain() + "\n" +
                "From: " + saved.getSource() + " To: " + saved.getDestination() + "\n" +
                "Coach: " + saved.getCoach() + " Seat: " + saved.getSeat() + "\n" +
                "Date: " + saved.getDate() + "\n\n" +
                "Thank you for booking with Railway Reservation System.";

        try {
            emailService.sendTicket(saved.getEmail(), "Train Ticket Confirmation", body);
        } catch (Exception ex) {
            System.out.println("Email failed: " + ex.getMessage());
        }

        model.addAttribute("booking", saved);
        return "ticket";
    }

    @GetMapping("/bookings")
    public String list(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "list";
    }

    @GetMapping("/ticket/pdf/{id}")
    public void downloadPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return;
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=ticket-" + booking.getId() + ".pdf");
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        document.add(new Paragraph("Indian Railways - E-Ticket"));
        document.add(new Paragraph("Booking ID: " + booking.getId()));
        document.add(new Paragraph("Passenger: " + booking.getName()));
        document.add(new Paragraph("Train: " + booking.getTrain()));
        document.add(new Paragraph("From: " + booking.getSource() + " To: " + booking.getDestination()));
        document.add(new Paragraph("Coach: " + booking.getCoach() + " Seat: " + booking.getSeat()));
        document.add(new Paragraph("Date: " + booking.getDate()));
        document.add(new Paragraph("Email: " + booking.getEmail()));
        document.close();
    }
}
