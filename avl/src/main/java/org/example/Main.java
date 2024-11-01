package org.example;
import java.util.*;
import java.util.function.Predicate;

interface Beholder<T> extends Iterable<T> {
    boolean leggInn(T t);    // Legger inn t i beholderen
    boolean inneholder(T t); // Sjekker om beholderen inneholder t
    boolean fjern(T t);      // Fjerner t fra beholderen
    int antall();            // Returnerer antall elementer i beholderen
    boolean tom();           // Sjekker om beholderen er tom
    void nullstill();        // Tømmer beholderen
    Iterator<T> iterator();  // Returnerer en iterator
}

class SøkeBinærTre<T> implements Beholder<T> {
    private class Node<T> {
        private T verdi; // Nodens verdi
        private Node<T> venstre, høyre, forelder; // Referanser til venstre barn, høyre barn og forelder
        private int hoyde;
        private Node(T verdi, Node<T> v, Node<T> h, Node<T> f) {
            this.verdi = verdi;
            this.venstre = v;
            this.høyre = h;
            this.forelder = f;
            this.hoyde = 1; // legger til hoyde
        }
        // Konstruktør som initialiserer noden med en verdi og forelder, venstre og høyre barn blir null
        private Node(T verdi, Node<T> f) {
            this(verdi, null, null, f); // Kaller den første konstruktøren med venstre og høyre barn som null
        }
        @Override
        public String toString() {return verdi.toString();}
    }
    private Node<T> rot;
    private int antall;
    private int endringer;
    private Comparator<? super T> comp;

    public SøkeBinærTre(Comparator<? super T> c) {
        this.comp = c;
        this.rot = null;
        this.antall = 0;
    }

    public boolean inneholder(T verdi) {
        if(verdi == null) return false;

        Node<T> p = rot;
        while(p != null) {
            int comparedValue = comp.compare(verdi,p.verdi);
            if(comparedValue < 0) p = p.venstre;
            else if (comparedValue > 0) p = p.høyre;
            else return true;
        }
        return false;
    }

    public int antall() {
        return antall;
    }
    public boolean tom() {
        return antall == 0;
    }

// start balansering og innsetting

    public boolean leggInn(T verdi) {
        Objects.requireNonNull(verdi, "Ikke tillat med null verdier");
        if (rot == null) {  // Hvis treet er tomt, opprett rot-noden
            rot = new Node<>(verdi, null, null, null);
            antall++;
            return true;
        }
        Node<T> akkuratNode = rot; // Start fra roten
        Node<T> forelder = null; // For å holde styr på foreldrenoden
        // Løkke for å finne riktig plassering
        while (akkuratNode != null) {
            forelder = akkuratNode; // Oppdaterer forelderen
            int comparedValue = comp.compare(verdi, akkuratNode.verdi); // Beregn sammenligningsverdi
            if (comparedValue < 0)
                akkuratNode = akkuratNode.venstre; // Gå venstre
            else if (comparedValue > 0)
                akkuratNode = akkuratNode.høyre;
            else
                akkuratNode = akkuratNode.høyre;
        }
        // Opprett en ny node med den funnet forelderen
        Node<T> nyNode = new Node<>(verdi, forelder);
        // Sett den nye noden til venstre eller høyre av forelderen
        if (comp.compare(verdi, forelder.verdi) < 0) {
            forelder.venstre = nyNode; // Legg til venstre
        } else {
            forelder.høyre = nyNode; // Legg til høyre
        }

        antall++; // Øk antall noder
        endringer++; // Oppdater endringer
        balanserForeldre(forelder); // Balanser treet etter innsetting
        return true; // Indikerer at innsetting var vellykket
    }
    private void balanserForeldre(Node node) {
        while (node != null) {
            // Oppdater høyden til noden
            node.hoyde = 1 + Math.max(hoyde(node.venstre),hoyde(node.høyre));
            //finn balanseDaktor
            int balanseFaktor = balanseFaktor(node);
            // Venstre venstre tilfelle, Gjør høyre rotasjon
            if(balanseFaktor > 1 && balanseFaktor(node.venstre) >= 0) {
                Node<T> nyRot = rightRotate(node);
                if(node == rot) {
                    rot = nyRot; // oppdater rot hvis nødvendig
                } else {
                    if(node.forelder.venstre == node) {
                        node.forelder.venstre = nyRot; // oppdater venstre barn av foreldrenoen
                    } else {
                        node.forelder.høyre = nyRot;
                    }
                }
                nyRot.forelder = node.forelder; // oppdater forelderen til nyRot
                node.forelder = nyRot; // oppdater forelder til node
                node = nyRot;
            }
            // Høyre høyre tilfelle
            else if (balanseFaktor < -1 && balanseFaktor(node.høyre) <= 0) {
                Node<T> nyRot = leftRotate(node);
                if(node == rot) {
                    rot = nyRot; // oppdater rot hvis nødvendig
                } else {
                    if(node.forelder.venstre == node) {
                        node.forelder.venstre = nyRot;
                    } else {
                        node.forelder.høyre = nyRot;
                    }
                }
                nyRot.forelder = node.forelder;
                node.forelder = nyRot;
                node = nyRot;
            }
            // Venstre høyre tilfelle
            else if(balanseFaktor > 1 && balanseFaktor(node.venstre) < 0) {
                node.venstre = leftRotate(node.venstre);
                Node<T> nyRot = rightRotate(node);
                node = nyRot;
                if(node == rot) {
                    rot = nyRot; // oppdater rot hvis nødvendig
                } else {
                    if(node.forelder.venstre == node) {
                        node.forelder.venstre = nyRot;
                    } else {
                        node.forelder.høyre = nyRot;
                    }
                }
                nyRot.forelder = node.forelder;
                node.forelder = nyRot;
                node = nyRot;
            }
            // Høyre venstre tilfelle
            else if(balanseFaktor < -1 && balanseFaktor(node.høyre) > 0) {
                node.høyre = rightRotate(node.høyre);
                Node<T> nyRot = leftRotate(node);
                if(node == rot) {
                    rot = nyRot;
                } else {
                    if(node.forelder.venstre == node) {
                        node.forelder.venstre = nyRot;
                    } else {
                        node.forelder.høyre = nyRot;
                    }
                }
                nyRot.forelder = node.forelder;
                node.forelder = nyRot;
                node = nyRot;
            }
            node = node.forelder;
        }
    }
    private Node rightRotate(Node y) {
        Node x = y.venstre;
        Node T2 = y.høyre;
        // rotere
        x.høyre = y;
        y.venstre = T2;
        // Oppdater høyder
        y.hoyde = Math.max(hoyde(y.venstre),hoyde(y.høyre)) + 1;
        x.hoyde = Math.max(hoyde(x.venstre),hoyde(x.høyre)) + 1;
        return x;// returner den nye roten for å opprettholde referanse
    }
    private Node leftRotate(Node y) {
        Node x = y.høyre;
        Node T2 = x.venstre;
        // rotasjon
        x.venstre = y;
        y.høyre = T2;
        // Oppdaterer høyder
        x.hoyde = Math.max(hoyde(x.venstre),hoyde(x.høyre)) + 1;
        y.hoyde = Math.max(hoyde(y.venstre),hoyde(y.høyre)) + 1;
        // returner den nye roten
        return x;
    }
    private int hoyde(Node node) {
        return (node == null) ? 0 : node.hoyde;
    }
    private int balanseFaktor(Node node) {
        return (node == null) ? 0 : hoyde(node.venstre) - hoyde(node.høyre);
    }


    // balanserign er ferdig
    public int antall(T verdi) {
        int teller = 0; // Initialiserer en teller for å holde oversikt over antall forekomster
        Node<T> akkuratNode = rot; // Starter fra rotnoden i treet

        while (akkuratNode != null) { // Så lenge det finnes noder i treet
            // Sjekker om både verdi og nodens verdi er null
            if (verdi == null && akkuratNode.verdi == null) {
                teller++; // Øker telleren hvis begge er null
                akkuratNode = akkuratNode.venstre;
                // Går til venstre barn antar at nullverdier er minst
            }
            // Hvis verdi ikke er null
            else if (verdi != null) {
                // Sammenligner verdien med nodens verdi
                int comparedValue = comp.compare(verdi, akkuratNode.verdi);
                if (comparedValue < 0) // Hvis verdien er mindre
                    akkuratNode = akkuratNode.venstre; // Gå til venstre barn
                else if (comparedValue > 0) // Hvis verdien er større
                    akkuratNode = akkuratNode.høyre; // Gå til høyre barn
                else { // Hvis verdiene er like
                    teller++; // Øker telleren for funnet verdi
                    akkuratNode = akkuratNode.høyre; // Gå til høyre barn for å se etter flere duplikater
                }
            } else {
                // Hvis verdien er null, men nodens verdi ikke er null
                akkuratNode = akkuratNode.venstre; // Gå til venstre barn (kan justeres basert på hvordan nullverdier håndteres)
            }
        }
        return teller; // Returnerer totalt antall forekomster av verdien
    }

    //prøver å finne den noden som er langt til venstre som mulig
    private Node<T> førstePostorden(Node<T> p) {
        // Sjekk om p er null. Hvis det er, returner null (ingen node å traversere).
        if (p == null) {
            return null;
        }
        // Start fra noden p og gå nedover i treet
        Node<T> current = p;
        // Fortsett til du når en bladnode (en node uten barn)
        while (current.venstre != null || current.høyre != null) {
            // Hvis noden har et venstre barn, gå til venstre
            if (current.venstre != null)
                current = current.venstre;

            else // gå til høyre hvis det finnes
                current = current.høyre;

        }
        return current; // Returner den første bladnoden i postorden-traverseringen
    }

    private Node<T> nestePostOrden(Node<T> p) {
        if(p == null) return null;

        Node<T> forelder = p.forelder;// Finn foreldrenoden til p
        if(forelder == null) {
            return null;
        }
        // Hvis p er høyre barn eller forelder har ingen høyre barn, gå til forelder
        if(forelder.høyre == p || forelder.høyre == null) {
            return forelder;
        }
        return førstePostorden(forelder.høyre);
    }

    @Override
    public boolean fjern(T verdi) {
        if(verdi == null) return false;

        Node<T> akkuratNode = rot;
        Node<T> forelder = null;

        int comparedValue = 0;

        while(akkuratNode != null) {
            comparedValue = comp.compare(verdi, akkuratNode.verdi);
            if(comparedValue < 0) {
                forelder = akkuratNode;
                akkuratNode = akkuratNode.venstre;
            } else if (comparedValue > 0){
                forelder = akkuratNode;
                akkuratNode = akkuratNode.høyre;
            } else
                break; // noden er funnet
        }
        if(akkuratNode == null) return false;

        // denne blir kun kjørt når akkuratNode har 2 barn
        if(akkuratNode.venstre != null && akkuratNode.høyre != null) {
            Node<T> inNode = akkuratNode.høyre; // må finne inorden node for å erstatte
            Node<T> inordenForelder = akkuratNode; // den  vi skal slette
            while(inNode.venstre != null) {
                inordenForelder = inNode;
                inNode = inNode.venstre;
            }
            akkuratNode.verdi = inNode.verdi; //erstatter noden som skal fjernes med en inorden node
            akkuratNode = inNode;
            forelder = inordenForelder; // forelder er en node før akkuratnode
        }

        Node<T> barn = (akkuratNode.venstre != null) ? akkuratNode.venstre : akkuratNode.høyre;
        // denne blir kun kjørt når noden som skal fjernes er rot-node
        if(akkuratNode == rot) {
            rot = barn;
        } else if (akkuratNode == forelder.venstre) { // denne blir kjørt når noden som skal fjernes er et venstre barn
            forelder.venstre = barn;
        } else { // denne blir kjørt når noden er høyre barn
            forelder.høyre = barn;
        }
        if(barn != null) { // sjekker om noden vi fjerner er barn, hvis det er det oppdater den forelder til å være akkuratNode sin forelder
            barn.forelder = forelder;
        }
        antall--;
        endringer++;
        return true;
    }

    //metode får å finne den minste noden i et tre (går alltid til venstre)
    public Node<T> finnMin(Node<T> node) {
        while (node.venstre != null) {
            node = node.venstre;
        }
        return node;
    }
    public int fjernAlle(T verdi) {
        if (verdi == null) return 0; // Ingen noder kan fjernes hvis verdien er null
        int fjernetAntall = 0; // Teller for antall noder som er fjernet
        while (true) { // Gjenta så lenge vi finner og fjerner noder
            boolean fjernet = fjern(verdi); // Prøv å fjerne noden
            if (!fjernet) break; // Hvis ingen node ble fjernet, avslutt løkken
            fjernetAntall++; // Øk teller for hver vellykket fjerning
            antall--;
            endringer++;
        }
        return fjernetAntall; // Returner antall fjernede noder
    }

    public int fjernAlleNodermedVerdi(T verdi) {
        if (verdi == null) return 0; // Ingen noder kan fjernes hvis verdien er null

        int fjernetAntall = 0; // Teller for antall noder som er fjernet
        Stack<Node> stack = new Stack<>();
        stack.push(rot); // Start med rot-noden

        while (!stack.isEmpty()) {
            Node<T> aktuellNode = stack.pop(); // Hent den øverste noden fra stacken

            if (aktuellNode == null) continue; // Hvis noden er null, gå til neste

            // Sjekk om noden har den verdien vi vil fjerne
            if (comp.compare(aktuellNode.verdi, verdi) == 0) {
                // Fjern noden
                fjern(aktuellNode.verdi); // Bruk den eksisterende fjern-metoden
                fjernetAntall++; // Øk antall fjernede noder
                endringer++;
                antall--;
            }

            // Legg til venstre og høyre barn til stacken
            if (aktuellNode.høyre != null) {
                stack.push(aktuellNode.høyre);
            }
            if (aktuellNode.venstre != null) {
                stack.push(aktuellNode.venstre);
            }
        }

        return fjernetAntall; // Returner antall fjernede noder
    }

    public String toStringPostorden() {
        if(tom()) return "[]";
        StringJoiner s = new StringJoiner(",","[","]");
        Node<T> p = førstePostorden(rot);
        while (p != null) {
            s.add(p.verdi.toString());
            p = nestePostOrden(p);
        }
        return s.toString();
    }

    // implementerer en inorden to string
    private Node<T> førsteInorden(Node<T> p) {
        if (p == null) return null;
        while (p.venstre != null) {
            p = p.venstre; // Gå til det venstre barnet til vi når bladet
        }
        return p;
    }

    private Node<T> nesteInorden(Node<T> p) {
        if (p == null) return null;
        // Hvis det finnes et høyre barn, finn det venstre mest i høyre subtre
        if (p.høyre != null) {
            return førsteInorden(p.høyre);
        }
        // Hvis ikke, gå opp til forelderen til vi er høyre barn
        Node<T> forelder = p.forelder;
        while (forelder != null && p == forelder.høyre) {
            p = forelder;
            forelder = forelder.forelder;
        }
        return forelder;
    }
    // inorden toString

    @Override
    public String toString() { //tostring inorden
        if (tom()) return "[]";
        StringJoiner s = new StringJoiner(",", "[", "]");

        Node<T> p = førsteInorden(rot); // Start med den første i inorden
        while (p != null) {
            s.add(p.verdi.toString());
            p = nesteInorden(p); // Gå til neste node i inorden
        }

        return s.toString();
    }




    // implementerer en preorden toString
    private Node<T> førstePreorden(Node<T> p) {
        return p; // Roten er alltid første i preorden
    }
    private Node<T> nestePreorden(Node<T> p) {
        if (p == null) return null;

        // Hvis venstre barn finnes, gå dit først
        if (p.venstre != null) {
            return p.venstre;
        }
        // Hvis ikke, og høyre barn finnes, gå til høyre
        if (p.høyre != null) {
            return p.høyre;
        }
        // Gå opp til forelderen til vi finner en node hvor vi kan gå til høyre
        Node<T> forelder = p.forelder;
        while (forelder != null) {
            if (forelder.venstre == p && forelder.høyre != null) {
                return forelder.høyre;
            }
            p = forelder;
            forelder = forelder.forelder;
        }

        return null; // Hvis vi ikke finner flere noder
    }
    public String toStringPreOrden() {
        if (tom()) return "[]";
        StringJoiner s = new StringJoiner(",", "[", "]");

        Node<T> p = førstePreorden(rot); // Start med første node i preorden (roten)
        while (p != null) {
            s.add(p.verdi.toString());
            p = nestePreorden(p); // Gå til neste node i preorden
        }

        return s.toString();
    }


    private class Stack<T> { // Legg til generisk typeparameter T
        private StackNode<T> top; // Peker til den øverste noden i stacken

        private class StackNode<T> { // Legg til generisk typeparameter T
            T data; // Dataen i noden, som er av den generiske typen T
            StackNode<T> next; // Peker til neste node i stacken

            StackNode(T data) {
                this.data = data; // Setter datafeltet til den gitte noden
                this.next = null; // Neste node er null ved opprettelse
            }
        }

        public void push(T data) { // Endre parameteren til den generiske typen T
            StackNode<T> nyNode = new StackNode<>(data); // Opprett en ny StackNode
            nyNode.next = top; // Den nye nodens neste node pekes til den nåværende toppen
            top = nyNode; // Oppdater toppen til å være den nye noden
        }

        public T pop() { // Endre returtypen til den generiske typen T
            if (top == null) return null; // Hvis stacken er tom, returner null

            T result = top.data; // Lagre dataen fra den øverste noden
            top = top.next; // Oppdater toppen til å peke på neste node (fjerner den øverste)
            return result; // Returner dataen som ble fjernet
        }

        public boolean isEmpty() {
            return top == null; // Returner true hvis toppen er null (stacken er tom)
        }
    }


    @Override
    public void nullstill() {
        if (rot == null) return; // Hvis treet allerede er tomt, gjør ingenting

        Stack<Node> stack = new Stack(); // Opprett en ny stack for å hjelpe oss å traversere treet
        stack.push(rot); // Start med rot-noden
        while (!stack.isEmpty()) { // Så lenge stacken ikke er tom
            Node aktuellNode = stack.pop(); // Ta den øverste noden fra stacken
            // Legg til venstre og høyre barn til stacken for videre behandling
            if (aktuellNode.venstre != null) {
                stack.push(aktuellNode.venstre);
            }
            if (aktuellNode.høyre != null) {
                stack.push(aktuellNode.høyre);
            }
            // Nullstill noden ved å fjerne referansene
            aktuellNode.venstre = null; // Nullstill venstre barn
            aktuellNode.høyre = null; // Nullstill høyre barn
            aktuellNode.forelder = null; // Nullstill forelder
        }
        rot = null; // Sett roten til null for å indikere at treet er tomt
        antall = 0; // Nullstill antall noder i treet
        endringer++; // Øk endringer for å indikere at treet har blitt endret
    }
    //inorder
    // @Override
    public String toString2() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        toStringPreOrder(rot, sb);
        if (sb.length() > 1) { // Hvis vi har lagt til verdier
            sb.setLength(sb.length() - 1); // Fjerne den siste kommaen
        }
        sb.append("]");
        return sb.toString();
    }

    private void toStringInOrder(Node<T> n, StringBuilder sb) {
        if (n == null) return;
        toStringInOrder(n.venstre, sb);
        sb.append(n.verdi.toString()).append(","); // Legg til verdien og komma
        toStringInOrder(n.høyre, sb);
    }
    private void toStringPreOrder(Node<T> n, StringBuilder sb) {
        if (n == null) return;
        sb.append(n.verdi.toString()).append(","); // Legg til verdien først
        toStringPreOrder(n.venstre, sb); // Gå til venstre subtre
        toStringPreOrder(n.høyre, sb); // Gå til høyre subtre
    }
    private void toStringPostOrder(Node<T> n, StringBuilder sb) {
        if (n == null) return;
        toStringPostOrder(n.venstre, sb); // Gå til venstre subtre
        toStringPostOrder(n.høyre, sb); // Gå til høyre subtre
        sb.append(n.verdi.toString()).append(","); // Legg til verdien sist
    }

    public Iterator<T> iterator() {
        return new InorderIterator();
    }
    private class InorderIterator implements Iterator<T> {
        // Stack for å holde noder under traversering
        private Stack<Node<T>> stack = new Stack<>();
        // Aktuell node som starter på roten av treet
        private Node<T> akkuratNode = rot;
        // Konstruktør som initierer iteratoren og starter med å trykke venstre noder på stacken
        public InorderIterator() {
            pushLeft(akkuratNode);
        }
        // Metode for å trykke alle venstre noder på stacken fra den gitte noden
        private void pushLeft(Node<T> node) {
            // Gå til den venstre noden så lenge den ikke er null
            while (node != null) {
                stack.push(node); // Legg til noden på stacken
                node = node.venstre; // Gå til venstre barn
            }
        }
        @Override
        public boolean hasNext() {
            return !stack.isEmpty(); // Returner true hvis stacken ikke er tom
        }
        // Overstyrt metode for å hente den neste verdien i in-order traverseringen
        public T next() {
            // Hvis det ikke er flere elementer, kast en NoSuchElementException
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Node<T> node = stack.pop(); // Ta den øverste noden fra stacken
            T verdi = node.verdi; // Lagre verdien til den noder som ble hentet
            // Hvis noden har et høyre barn, trykk venstre noder fra den høyre noden
            if (node.høyre != null) {
                pushLeft(node.høyre);
            }
            return verdi; // Returner verdien
        }
    }


}


public class Main {
    public static void main(String[] args) {
        Comparator<Integer> c = Comparator.naturalOrder();
        SøkeBinærTre<Integer> bn = new SøkeBinærTre<>(c);
        int[] liste = {1,3,0,3,-6,4,5};
        for(int i : liste) bn.leggInn(i);
        System.out.println(bn.toString());
        for(Integer i : bn) System.out.println(i);
    }
}
