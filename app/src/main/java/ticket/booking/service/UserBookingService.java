package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserBookingService {

    private List<User> userList;
    private User user;
    private static final String USER_INFO_PATH = "app/src/main/java/ticket/booking/localDb/users.json";
    private final ObjectMapper objectMapper = new ObjectMapper();


    public UserBookingService() throws IOException{
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        loadUserListFromFile();
    }

    public UserBookingService(User user) throws IOException {
        this.user = user;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        loadUserListFromFile();
    }

    public void loadUserListFromFile() throws IOException{
        File jsonFile = new File(USER_INFO_PATH);
        userList = objectMapper.readValue(jsonFile, new TypeReference<List<User>>() {});
    }

    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream()
                .filter( user1 -> user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getPassword()))
                .findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1){
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException ioe){
            return Boolean.FALSE;
        }
    }

    public void fetchBookings(){
        Optional<User> userFetched = userList.stream().filter(user1 -> {
                return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        userFetched.ifPresent(User::printTickets);
    }

    public Boolean cancelBooking(String ticketId){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Ticket ID : ");
        ticketId = sc.next();

        if(ticketId != null && ticketId.isEmpty()){
            System.out.println("Ticket ID cannot be empty!!!");
            return Boolean.FALSE;
        }

        String finalTicketId = ticketId;
        boolean removed = user.getTicketsBooked().removeIf(tkt -> tkt.getTicketId().equals(finalTicketId));

        if (removed){
            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            return Boolean.TRUE;
        }
        else {
            System.out.println("No Ticket Found With ID : " + ticketId);
            return Boolean.FALSE;
        }
    }

    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        }catch(IOException ex){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat){
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();

            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()){
                if (seats.get(row).get(seat) == 0){
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return Boolean.TRUE;    // successfully seat booked
                }
                else {
                    return Boolean.FALSE;   // seat already allotted
                }
            }
            else {
                return Boolean.FALSE;   // invalid seat or row
            }
        } catch (IOException iox){
            return Boolean.FALSE;
        }
    }


    private void saveUserListToFile() throws IOException{
        objectMapper.writeValue(new File(USER_INFO_PATH), userList);
    }

}
