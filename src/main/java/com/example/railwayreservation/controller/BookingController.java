package com.example.railwayreservation.controller;

import com.example.railwayreservation.entity.Booking;
import com.example.railwayreservation.repository.BookingRepository;
import com.example.railwayreservation.service.EmailService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public BookingController(BookingRepository bookingRepository,
                             EmailService emailService) {
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
    public List<Map<String, String>> trains(@RequestParam String source,
                                            @RequestParam String destination) {

        String key = source.trim().toLowerCase() + "-" + destination.trim().toLowerCase();
        Map<String, List<String>> routeTrains = buildTrainMap();

        List<String> names = routeTrains.getOrDefault(
                key,
                routeTrains.getOrDefault(destination.toLowerCase() + "-" + source.toLowerCase(), List.of())
        );

        if (names.isEmpty()) {
            names = List.of("Superfast Express", "Jan Shatabdi", "Passenger Special");
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

    // 🔒 LOGIN REQUIRED BOOKING (STEP 6)
    @PostMapping("/book")
    public String book(Booking booking,
                       Model model,
                       HttpSession session) {

        // 🚨 SESSION CHECK
        if (session.getAttribute("loggedUser") == null) {
            return "redirect:/login";
        }

        Random r = new Random();
        if (booking.getCoach() == null || booking.getCoach().isBlank()) {
            String[] coaches = {"A1", "A2", "B1", "B2", "S1", "S2"};
            booking.setCoach(coaches[r.nextInt(coaches.length)]);
        }

        if (booking.getSeat() == null || booking.getSeat().isBlank()) {
            booking.setSeat(String.valueOf(r.nextInt(72) + 1));
        }

        Booking saved = bookingRepository.save(booking);

        String body = "Hello " + saved.getName() + ",\n\n" +
                "Your ticket is confirmed!\n" +
                "Train: " + saved.getTrain() + "\n" +
                "From: " + saved.getSource() + " To: " + saved.getDestination() + "\n" +
                "Coach: " + saved.getCoach() + " Seat: " + saved.getSeat() + "\n" +
                "Date: " + saved.getDate() + "\n\n" +
                "Thank you for booking.";

        emailService.sendTicket(
                saved.getEmail(),
                "Train Ticket Confirmation",
                body
        );

        model.addAttribute("booking", saved);
        return "ticket";
    }

    @GetMapping("/bookings")
    public String list(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "list";
    }

    @GetMapping("/ticket/pdf/{id}")
    public void downloadPdf(@PathVariable Long id,
                            HttpServletResponse response) throws IOException {

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return;

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=ticket-" + booking.getId() + ".pdf"
        );

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

    // Train data
    private Map<String, List<String>> buildTrainMap() {
        Map<String, List<String>> m = new HashMap<>();
        m.put("delhi-mumbai", List.of("Rajdhani Express", "Duronto Express", "Garib Rath"));
        m.put("mumbai-delhi", List.of("Rajdhani Express", "Duronto Express"));
        return m;
    }
}
