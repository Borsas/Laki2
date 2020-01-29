import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class BimEdit {
    // errorMsg tulostetaan jos kuva tiedostossa on jokin virhe.
    public static final String errorMsg = "Invalid image file!";

    public static void main(String[] args) {
        System.out.print("-----------------------\n| Binary image editor |\n-----------------------\n");

        // Tarkistetaanko onko argumenteissa mitään ylimääräistä tai virheitä
        if(args.length == 2){
            if(args[0].equals("echo") && args[1].contains(".txt")){
                mainLoop(args);
            }else{
                System.out.println("Invalid command-line argument!");
            }
        }else if(args.length == 1){
            if(args[0].contains(".txt")){
                mainLoop(args);
            }else{
                System.out.println("Invalid command-line argument!");
            }
        }
        else{
            System.out.println("Invalid command-line argument!");
        }
        System.out.println("Bye, see you soon.");
    }


    /** Ohjelman ns. päälooppi, kaikki komentojen lukeminen yms tapahtuu täällä ja method kutsutaan mainissa
     * jos agumenteissa ei ole mitään ylimääräistä tai virheitä
     */
    public static void mainLoop(String[] args){
        // Ohjelman main loop
        Scanner scanner = new Scanner(System.in);

        String komennot = "print/info/invert/dilate/erode/load/quit?";
        String imageFile = args[0];
        boolean echoEnabled = false;

        // Tarkistetaan onko echo annettu argumenttina ohjelmalle
        if(args[0].equals("echo")){
            imageFile = args[1];
            echoEnabled = true;
        }
        // imageFileData sisältää tiedoston kaikki rivit
        String[] imageFileData = readFile(imageFile);
        boolean main = true;

        if (imageFileData != null) {
            // image on 2d char array joka sisältää halutun kuvan.
            char[][] image = parseImage(imageFileData);

            while (main) {
                System.out.println(komennot);

                String input = scanner.nextLine();

                // Pitää ensin tarkistaa onko kommennossa numeroita perässä dilatea ja erodea varten,
                // jos ei ole jatketaa toiseen switch caseen.
                if (input.contains(" ")) {
                    String[] command = input.split(" ");
                    try {
                        int size = Integer.parseInt(command[1]);

                        // Tarkistetaanko onko inputissa joitakin ylimääräisiä merkkejä
                        if (size % 2 == 0 || command.length != 2 || size < 3 ||
                            size > Integer.parseInt(imageFileData[0]) || size > Integer.parseInt(imageFileData[1])){
                            System.out.println("Invalid command!");
                        }else {
                            String komento = command[0];

                            // Tarkistaa onko komento oikea ja echo parametri annettu ohjelmalle
                            if (komennot.contains(komento) && echoEnabled) {
                                System.out.println(input);
                            }

                            switch (komento) {
                                case "dilate":
                                    image = dilateImage(image, size, imageFileData[3].charAt(0));
                                    break;
                                case "erode":
                                    image = dilateImage(image, size, imageFileData[2].charAt(0));
                                    break;
                                default:
                                    System.out.println("Invalid command!");
                            }
                        }
                        // Jos komennossa ei olekkaan oikeata numeroa
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        System.out.println("Invalid command!");
                    }
                } else {
                    // Tarkistaa onko komento oikea ja echo parametri annettu ohjelmalle
                    // ja ettei input ole dilate tai erode
                    if (komennot.contains(input) && echoEnabled && !"dilate erode".contains(input)) {
                        System.out.println(input);
                    }

                    // Tämä switch ottaa vastaan halutut komennot ja ajaa niitä vastaavat funktiot
                    // Jos komento on väärin printtaa switch defaulttina "Invalid command!"
                    switch (input) {
                        case "print":
                            printImage(image);
                            break;
                        case "info":
                            printInfo(imageFileData, image);
                            break;
                        case "invert":
                            invertImage(image, imageFileData);
                            break;
                        case "load":
                            imageFileData = readFile(imageFile);
                            image = parseImage(imageFileData);
                            break;
                        case "quit":
                            main = false;
                            break;
                        default:
                            System.out.println("Invalid command!");
                            break;
                    }
                }
            }
        } else {
            System.out.println(errorMsg);
        }
    }

    /** Kopioi tiedot 2D Character arraysta toiseen arrayhin ja palauttaa sen*/
    public static char[][] copyCharArray(char[][] firstArray){
        int firstSize = firstArray.length;
        int secondSize = firstArray[0].length;

        char[][] secondArray = new char[firstSize][secondSize];

        for (int i = 0; i < firstSize; i++) {
            for (int j = 0; j < secondSize; j++) {
                secondArray[i][j] = firstArray[i][j];
            }
        }
        return secondArray;
    }

    /** Dilatee tai erodee 2D char arrayn luoden ensin siitä kopion
     method myös palauttaa kyseisen kopion alkuperäisen sijasta
     method tarvitsee 2d char array, "ikkunan" koon ja edustamerkin*/
    public static char[][] dilateImage(char[][] image, int size, char mark){
        // size on aluksi haluttu alue eli esim 3 tai 5, joka josta vähennetään 1 ja se jaetaan kahdella
        // näin saadaan arvo josta for loopit voidaan aloittaa ja lopettaa, eli ns. reunan paksuus
        size = (size - 1) / 2;

        // Ottaa kopion alkuperäisestä image arraysta
        char[][] newImage = copyCharArray(image);

        // Ensimmäiset 2 for looppia käyvät läpi 2d char arrayn
        // Toiset 2 for looppia tarkistavat onko etäisyydellä size +1 etsittyä merkkiä eli 'char mark'
        // jos mark löytyy halutulta alueelta merkataan dilateImage 2d arrayhin [i][j] koordinaatin kohdalle char mark

        for (int i = size; i < image.length - size; i++) {
            for (int j = size; j < image[0].length - size; j++) {

                for(int k = i - size; k < i + size + 1; k++) {
                    for(int l = j - size; l < j + size + 1; l++)  {

                        if(image[k][l] == mark){
                            newImage[i][j] = mark;
                            break;
                        }

                    }
                }
            }
        }
        return newImage;
    }

    /** Kääntää kuvan merkit ympäri **/
    public static void invertImage(char[][] image, String[] data){
        int size = image.length;
        String taustaChar = data[2];
        String edustaChar = data[3];

        for(int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Tämä if tarkistaa ovatko merkit image[i][j] ja data[2] (Eli txt tiedostossa kerrottu merkki)
                // samat, jos ovat niin image[i][j] on nyt data[3] eli toinen merkki, jos ovat eri
                // image[i][j] on nyt data[2]
                if(image[i][j] == data[2].charAt(0)){
                    image[i][j] = data[3].charAt(0);
                }else{
                    image[i][j] = data[2].charAt(0);
                }
            }
        }
        data[3] = taustaChar;
        data[2] = edustaChar;

    }

    /** Tuottaa muistiin 2d char arrayn ja palauttaa sen
     kuva saadaan parseemalla data[4] --> eteenpäin tiedot
     rivi kerrallaa
     **/
    public static char[][] parseImage(String[] data){
        int firstSize = Integer.parseInt(data[0]);
        int secondSize = Integer.parseInt(data[1]);
        char[][] image = new char[firstSize][secondSize];

        // Lukee tiedostosta otetun kuva datan ja tekee siitä 2D char arrayn.
        for(int i = 4; i < firstSize + 4; i++){
            for(int j = 0; j < secondSize; j++){
                image[i - 4][j] = data[i].charAt(j);
            }
        }
        return image;
    }

    /** Tulostaa kuvan **/
    public static void printImage(char[][] image){
        int size = image[0].length;

        // Käy merkit läpi ja printtaa merkit sijainnista image[i][j]
        for (char[] chars : image) {
            for (int j = 0; j < size; j++) {
                System.out.print(chars[j]);
            }
            System.out.println();
        }
    }

    /** Printtaa tiedot kuvasta järjestyksessä
     * kuvan koko N x N
     * taustamerkkin lukumäärä
     * edustamerkkien lukumäärä
     **/
    public static void printInfo(String[] data, char[][] image){
        int taustaMerkki = 0;
        int edustaMerkki = 0;
        int firstSize = Integer.parseInt(data[0]);
        int secondSize = Integer.parseInt(data[1]);


        for(int i = 0; i < firstSize; i++){
            for(int j = 0; j < secondSize; j++){
                if(image[i][j] == (data[2]).charAt(0)){
                    taustaMerkki++;
                }else{
                    edustaMerkki++;
                }
            }
        }
        System.out.printf("%s x %s\n", data[0], data[1]);
        System.out.printf("%s %d\n", data[2], taustaMerkki);
        System.out.printf("%s %d\n", data[3], edustaMerkki);
    }

    /** Tarkistetaan onko alkuperäisessä tiedostossa jotain virheitä joita ei saisi tapahtua, palauttaa
     * false jos ei ongelmia, true jos ongelmia ilmenee
     */
    public static boolean checkErrors(int lineCounter, String line, String[] imageFileData){
         // Tarkistetaan että rivit ovat 3 tai pidempiä
        if(Integer.parseInt(imageFileData[0]) < 3) {
            return true;
        }else if(lineCounter > 2 && Integer.parseInt(imageFileData[1]) < 3){
            return false;

         // Kun lineCounter on yli 4, alkaa tiedostossa kuva, tämä kohta funktiota tarkistaa onko joka ikinen
         // rivi saman mittainen kuin pitäisi vertaamalla rivin pituutta imageFileData[0] kohtaan muistissa
         // jossa sijaitsee pituus.
        }else if(lineCounter > 4){
            if(line.length() != Integer.parseInt(imageFileData[1])){
                return true;

            // Jos rivit ovat oikean mittaiset, tämä kohta koodissa tarkistaa sisältääkö rivit pelkästään
            // oikeita merkkejä jotka on määritetty muistipaikkoihin imageFileData[2] ja [3].
            }else{
                String[] row = line.split("");
                for(String i : row){
                    if(!i.equals(imageFileData[2]) && !i.equals(imageFileData[3])){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Luetaan tiedosto ja tallennetaan kaikki sen rivit String arrayhin, aluksi luetaan vain ensimmäinen rivi, josta
     * saadaan selville tarvittavan arrayin pituus. Jos tiedostossa on virheitä palauttaa readFile null.
     */
    public static String[] readFile(String imageFile) {
        File tiedosto = new File(imageFile);
        // imageFileData sisältää tiedoston datan ja ne on sijoitettu seuraaviin indexeihin:
        // 0: rivien lukumäärä
        // 1: sarakkeiden lukumäärä
        // 2: taustamerkki
        // 3: edustamerkki
        // 4-x: muut rivit, toinen method iteroi tiedot paremmin.
        if (tiedosto.exists()) {
            try {
                Scanner fileInput = new Scanner(tiedosto);

                // Lukee ensimmäiseltä riviltä kuvan rivien pituuden, jotta voimme luoda oikean kokoisen arrayn
                String line = fileInput.nextLine();
                String[] imageFileData = new String[Integer.parseInt(line) + 4];
                imageFileData[0] = line;

                int lineCounter = 1;
                while (fileInput.hasNextLine()) {
                    line = fileInput.nextLine();

                    // Tarkistaa onko tiedostossa virheitä
                    if (checkErrors(lineCounter, line, imageFileData)) {
                        return null;
                    }
                    // Asettaa rivin imageFileDataan ja kasvattaa lineCounter yhdellä.
                    imageFileData[lineCounter] = line;
                    lineCounter++;
                }
                if (lineCounter - 4 != Integer.parseInt(imageFileData[0])) {
                    return null;
                }
                return imageFileData;

            } catch (FileNotFoundException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return null;
            }
        } return null;
    }
}
