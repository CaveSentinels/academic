public class SparksPlay {
    public static void main(String[] args) {
        String str = ("Juan Carlos Portantiero (Buenos Aires, 1934 – Buenos Aires, 9 March 2007) was an Argentine sociologist, specializing in the study of the works of Antonio Gramsci. \\n\\nHe graduated in Sociology from the Universidad de Buenos Aires (UBA), and went into exile during the Proceso Militar (1976–1983) because of threats received. He relocated to Mexico, where he founded the Controversia journal.\\n\\nAfter the return of democracy, he become one of the most respected Argentine scholars and had a direct influence on politics as an advisor to Unión Cívica Radical president Raúl Alfonsín and member of the advising team dubbed Grupo Esmeralda.\\n\\nHe acted as elected dean of UBA's Social Sciences School (1990–1998).\\n\\n \\n\\n\t").replaceAll("\\\\n", " ");
        String[] parts1 = str.split("\\s");
        String[] parts2 = str.split("\\s+");

        System.out.println("==========");
        for (String part : parts1) {
            System.out.print(part + "|");
        }
        System.out.println("");
        System.out.println(parts1.length);

        System.out.println("==========");
        for (String part : parts2) {
            System.out.print(part + "|");
        }
        System.out.println("");
        System.out.println(parts2.length);
    }
}
