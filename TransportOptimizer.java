import java.util.*;
import java.io.File; 
import java.io.FileNotFoundException;

class City {
    private int id;  
    private String name;
    private int population;

    public City(int id, String name, int population) {
        this.id = id;  
        this.name = name;
        this.population = population;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPopulation() {
        return population;
    }

    @Override
    public String toString() {
        return name + " (Population : " + population + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof City))
            return false;
        City other = (City) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

class Route {
    private City source;
    private City destination;
    private int distance;
    private int time;

    public Route(City source, City destination, int distance, int time) {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.time = time;
    }

    public City getSource() {
        return source;
    }

    public City getDestination() {
        return destination;
    }

    public int getDistance() {
        return distance;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return source.getName() + " to " + destination.getName() + " - " + distance + "km in " + time + " mins";
    }
}

class Graph {
    private Map<Integer, City> cities = new HashMap<>();
    private Map<City, List<Route>> adjList = new HashMap<>();

    public void addCity(City city) {
        cities.put(city.getId(), city);
        adjList.put(city, new ArrayList<>());
    }

    public City getCityById(int id) {
        return cities.get(id);
    }

    public void addRoute(Route route) {
        adjList.get(route.getSource()).add(route);
        Route reverseRoute = new Route(route.getDestination(), route.getSource(), route.getDistance(), route.getTime());
        adjList.get(route.getDestination()).add(reverseRoute);
    }

    public List<Route> getRoutesFromCity(City city) {
        return adjList.get(city);
    }

    public List<City> getCities() {
        return new ArrayList<>(cities.values());
    }

    public Route getRoute(City source, City destination) {
        return adjList.get(source).stream().filter(route -> route.getDestination().equals(destination)).findFirst()
                .orElse(null);
    }

    public void removeCity(City city) {
        if (cities.containsKey(city.getId())) {
            cities.remove(city.getId());
            adjList.remove(city);
            for (List<Route> routes : adjList.values()) {
                routes.removeIf(route -> route.getSource().equals(city) || route.getDestination().equals(city));
            }
        }
    }

    public void removeRoute(City source, City destination) {
        List<Route> routesFromSource = adjList.get(source);
        if (routesFromSource != null) {
            routesFromSource.removeIf(route -> route.getDestination().equals(destination));
        }
        List<Route> routesFromDestination = adjList.get(destination);
        if (routesFromDestination != null) {
            routesFromDestination.removeIf(route -> route.getDestination().equals(source));
        }
    }

}

class Dijkstra {
    private Graph graph;

    public Dijkstra(Graph graph) {
        this.graph = graph;
    }

    public List<City> findShortestRoute(City start, City end) {
        Map<City, City> predecessors = new HashMap<>();
        Map<City, Integer> distances = new HashMap<>();
        PriorityQueue<City> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        Set<City> explored = new HashSet<>();

        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            City current = queue.poll();
            explored.add(current);

            for (Route route : graph.getRoutesFromCity(current)) {
                City neighbor = route.getDestination();
                int newDist = distances.get(current) + route.getDistance();

                if (!distances.containsKey(neighbor) || newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    predecessors.put(neighbor, current);

                    if (explored.contains(neighbor)) {
                        queue.remove(neighbor);
                    }
                    queue.add(neighbor);
                }
            }
        }

        List<City> path = new ArrayList<>();
        for (City at = end; at != null; at = predecessors.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}

class Ticket {
    private static int counter = 0;
    private int ticketId;
    private City source;
    private City destination;
    private int fare;
    private Date date;

    public Ticket(City source, City destination, int fare) {
        this.ticketId = ++counter;
        this.source = source;
        this.destination = destination;
        this.fare = fare;
        this.date = new Date();
    }

    public int getFare() {
        return fare;
    }

    public int getTicketId() {
        return ticketId;
    }

    public City getSource() {
        return source;
    }

    public City getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "Ticket ID: " + ticketId + ", From: " + source.getName() + ", To: " + destination.getName() + ", Fare: $" + fare + ", Date: " + date;
    }
}

public class TransportOptimizer {
    private static Graph graph = new Graph();
    private static Scanner sc = new Scanner(System.in);
    private static List<Ticket> tickets = new ArrayList<>();

    private static void readCitiesFromFile(String filePath) {
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                Scanner lineScanner = new Scanner(line);

                int cityId = lineScanner.nextInt();
                String cityName = lineScanner.next();
                int population = lineScanner.nextInt();

                City city = new City(cityId, cityName, population);
                graph.addCity(city);

                lineScanner.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
    }

    private static void readRoutesFromFile(String filePath) {
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                Scanner lineScanner = new Scanner(line);

                int sourceCityId = lineScanner.nextInt();
                int destCityId = lineScanner.nextInt();
                int distance = lineScanner.nextInt();
                int time = lineScanner.nextInt();

                City sourceCity = graph.getCityById(sourceCityId);
                City destCity = graph.getCityById(destCityId);

                if (sourceCity != null && destCity != null) {
                    Route route = new Route(sourceCity, destCity, distance, time);
                    graph.addRoute(route);
                } else {
                    System.out.println("Invalid city ID in route data");
                }

                lineScanner.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
    }

    private static void printRoutesFromCity(City city) {
        System.out.println("Routes from " + city + ":");
        for (Route route : graph.getRoutesFromCity(city)) {
            System.out.println(route);
        }
    }

    private static void addNewCity() {
        System.out.println("Enter City Name:");
        String name = sc.next();
        System.out.println("Enter Population:");
        int population = sc.nextInt();
        City city = new City(graph.getCities().size() + 1, name, population);
        graph.addCity(city);
        System.out.println("City added successfully!");
    }

    private static void addNewRoute() {
        System.out.println("Available Cities:");
        for (City city : graph.getCities()) {
            System.out.println(city.getId() + ". " + city.getName());
        }
        System.out.println("Select Source City ID:");
        int sourceId = sc.nextInt();
        System.out.println("Select Destination City ID:");
        int destId = sc.nextInt();
        System.out.println("Enter Distance (in km):");
        int distance = sc.nextInt();
        System.out.println("Enter Time (in mins):");
        int time = sc.nextInt();
        Route route = new Route(graph.getCityById(sourceId), graph.getCityById(destId), distance, time);
        graph.addRoute(route);
        System.out.println("Route added successfully!");
    }

    private static void deleteTicket() {
        System.out.println("Enter Ticket ID to delete:");
        int ticketId = sc.nextInt();
        for (Iterator<Ticket> iterator = tickets.iterator(); iterator.hasNext();) {
            Ticket ticket = iterator.next();
            if (ticket.getTicketId() == ticketId) {
                iterator.remove();
                System.out.println("Ticket deleted successfully!");
                return;
            }
        }
        System.out.println("Ticket ID not found.");
    }

    private static void findShortestRoute() {
        System.out.println("Available Cities:");
        for (City city : graph.getCities()) {
            System.out.println(city.getId() + ". " + city.getName());
        }
        System.out.println("Select Start City ID:");
        int startId = sc.nextInt();
        System.out.println("Select End City ID:");
        int endId = sc.nextInt();
        Dijkstra dijkstra = new Dijkstra(graph);
        List<City> route = dijkstra.findShortestRoute(graph.getCityById(startId), graph.getCityById(endId));
        System.out.println("Shortest Route:");
        for (int i = 0; i < route.size(); i++) {
            System.out.print(route.get(i).getName());
            if (i < route.size() - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println();
    }

    private static int calculateFare(City source, City destination) {
        Route route = graph.getRoute(source, destination);
    
        if (route != null) {
            int baseFare = route.getDistance() * 10;
            int demandSurcharge = (source.getPopulation() + destination.getPopulation()) / 1000000;
            return baseFare + demandSurcharge;
        } else {
            System.out.println("No route found between " + source.getName() + " and " + destination.getName());
            return -1; // or handle the situation as needed in your application
        }
    }

    private static void bookTicket() {
        System.out.println("Available Cities:");
        for (City city : graph.getCities()) {
            System.out.println(city.getId() + ". " + city.getName());
        }
        System.out.println("Select Start City ID:");
        int startId = sc.nextInt();
        System.out.println("Select End City ID:");
        int endId = sc.nextInt();
        City source = graph.getCityById(startId);
        City destination = graph.getCityById(endId);
        int fare = calculateFare(source, destination);
        tickets.add(new Ticket(source, destination, fare));
        System.out.println("Ticket booked successfully! Fare: $" + fare);
    }

    private static void viewTickets() {
        System.out.println("All booked tickets:");
        tickets.stream().sorted(Comparator.comparingInt(Ticket::getFare).reversed())
                .forEach(System.out::println);
    }

    private static void deleteCity() {
        System.out.println("Enter City ID to delete:");
        int cityId = sc.nextInt();
        City city = graph.getCityById(cityId);
        if (city != null) {
            tickets.removeIf(ticket -> ticket.getSource().equals(city) || ticket.getDestination().equals(city));
            graph.removeCity(city);
            System.out.println("City deleted successfully!");
        } else {
            System.out.println("City ID not found.");
        }
    }

    private static void deleteRoute() {
        System.out.println("Available Cities:");
        for (City city : graph.getCities()) {
            System.out.println(city.getId() + ". " + city.getName());
        }
        System.out.println("Select Source City ID:");
        int sourceId = sc.nextInt();
        System.out.println("Select Destination City ID:");
        int destId = sc.nextInt();

        City source = graph.getCityById(sourceId);
        City destination = graph.getCityById(destId);
        if (source != null && destination != null) {
            graph.removeRoute(source, destination);
            System.out.println("Route deleted successfully!");
        } else {
            System.out.println("City ID not found.");
        }
    }

    public static void main(String[] args) {
        readCitiesFromFile("cities.txt");
        readRoutesFromFile("routes.txt");
        while (true) {
            System.out.println("\nTransport Optimizer Menu:");
            System.out.println("1. Add New City");
            System.out.println("2. Add New Route");
            System.out.println("3. Find Shortest Route");
            System.out.println("4. Book a Ticket");
            System.out.println("5. View All Tickets");
            System.out.println("6. View All Cities");
            System.out.println("7. View All Routes");
            System.out.println("8. Delete a City");
            System.out.println("9. Delete a Route");
            System.out.println("10. Delete a Ticket");
            System.out.println("11. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    addNewCity();
                    break;
                case 2:
                    addNewRoute();
                    break;
                case 3:
                    findShortestRoute();
                    break;
                case 4:
                    bookTicket();
                    break;
                case 5:
                    viewTickets();
                    break;
                case 6:
                    graph.getCities().forEach(System.out::println);
                    break;
                case 7:
                    graph.getCities().forEach(city -> printRoutesFromCity(city));
                    break;
                case 8:
                    deleteCity();
                    break;
                case 9:
                    deleteRoute();
                    break;
                case 10:
                    deleteTicket();
                    break;
                case 11:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}