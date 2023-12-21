import org.apache.commons.math3.util.Pair;

import java.util.*;

public class Day20 {

    public static void main(String[] args) {
        new Day20().doChallenge();
    }

    private void doChallenge() {
        String input = getInput();
        doPart1(input);
        doPart2(input);
    }

    private void doPart1(String input) {
        Module broadcastModule = createModuleGraph(input);
        for (int i = 0; i < 1000; i++) {
            broadcastModule.triggerPulse(null, false);
        }
        System.out.println("Part 1: "+ Module.lowPulses * Module.highPules);
    }

    private void doPart2(String input) {
        System.out.println("Part 2: ...screw part 2");
    }

    private Module createModuleGraph(String input) {
        Scanner sc = new Scanner(input);

        // Record all the modules as pairs of {nodeName, destinationNames}.
        List<Pair<String, List<String>>> nameAndDestinations = new ArrayList<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Scanner sc2 = new Scanner(line);
            sc2.useDelimiter(" ");
            String nodeName = sc2.next();
            List<String> destinationNodes = new ArrayList<>();
            sc2.next(); // ->
            while (sc2.hasNext()) {
                String destination = sc2.next();
                if (destination.endsWith(",")) {
                    destination = destination.substring(0, destination.length() - 1);
                }
                destinationNodes.add(destination);
            }
            nameAndDestinations.add(new Pair<>(nodeName, destinationNodes));
        }

        // Create placeholder modules for each nodeName
        Map<String, Module> nameToNode = new HashMap<>();
        for (var stringModuleEntry : nameAndDestinations) {
            String moduleName = stringModuleEntry.getKey();
            if (moduleName.startsWith("%")) {
                nameToNode.put(moduleName.substring(1), new FlipFlopModule());
            } else if (moduleName.startsWith("&")) {
                nameToNode.put(moduleName.substring(1), new ConjunctionModule());
            } else if (moduleName.equals("broadcaster")) {
                nameToNode.put(moduleName, new BroadcastModule());
            }
        }

        // Go through and make sure each module has the correct destinations and sources set.
        for (var nameAndDestination : nameAndDestinations) {
            String moduleName = nameAndDestination.getKey();
            List<String> moduleDestinations = nameAndDestination.getValue();
            if (moduleName.startsWith("%") || moduleName.startsWith("&")) {
                moduleName = moduleName.substring(1);
            }
            Module m = nameToNode.get(moduleName);
            for (String dest : moduleDestinations) {
                if (dest.startsWith("%") || dest.startsWith("&")) {
                    dest = dest.substring(1);
                }
                Module d = nameToNode.containsKey(dest) ? nameToNode.get(dest) : new EmptyModule();
                m.destinationModules.add(d);
                if (d instanceof ConjunctionModule) {
                    ((ConjunctionModule) d).inputModules.add(m);
                    ((ConjunctionModule) d).mostRecentPulses.add(false);
                }
            }
        }

        // return the root node (the broadcaster node)
        return nameToNode.get("broadcaster");
    }

    private abstract class Module {

        private static long lowPulses = 0;
        private static long highPules = 0;

        List<Module> destinationModules = new ArrayList<>();

        abstract void triggerPulse(Module triggeringModule, boolean recievedPulse);

        void recordPulse(boolean pulse) {
            if (pulse) {
                highPules++;
            } else {
                lowPulses++;
            }
        }
    }

    private class EmptyModule extends Module {

        @Override
        public String toString() {
            return "*";
        }

        @Override
        void triggerPulse(Module triggeringModule, boolean receivedPulse) {
            super.recordPulse(receivedPulse);
        }
    }

    private class BroadcastModule extends Module {

        void triggerPulse(Module triggeringModule, boolean receivedPulse) {
            super.recordPulse(receivedPulse);
            destinationModules.forEach(m -> m.triggerPulse(this, receivedPulse));
        }

        @Override
        public String toString() {
            return "^";
        }
    }

    private class ConjunctionModule extends Module {
        List<Module> inputModules = new ArrayList<>();
        List<Boolean> mostRecentPulses = new ArrayList<>();

        @Override
        void triggerPulse(Module triggeringModule, boolean receivedPulse) {
            super.recordPulse(receivedPulse);
            int indexOfInput = inputModules.indexOf(triggeringModule);
            mostRecentPulses.set(indexOfInput, receivedPulse);

            boolean pulsesTogether = true;
            for (Boolean p : mostRecentPulses) {
                pulsesTogether &= p;
            }

            boolean pulseToSend = !pulsesTogether;
            destinationModules.forEach(m -> m.triggerPulse(this, pulseToSend));
        }
        @Override
        public String toString() {
            return "&";
        }
    }

    private class FlipFlopModule extends Module {

        private boolean status = false;

        @Override
        void triggerPulse(Module triggeringModule, boolean receivedPulse) {
            super.recordPulse(receivedPulse);
            if (!receivedPulse) {
                status = !status;
                destinationModules.forEach(m -> m.triggerPulse(this, status));
            }
        }

        @Override
        public String toString() {
            return "%";
        }
    }

    private String getInput2() {
        return """
                broadcaster -> a, b, c
                %a -> b
                %b -> c
                %c -> inv
                &inv -> a
                """;
    }

    private String getInput() {
        return """
                %qp -> ng, pl
                %mq -> zz
                %lq -> zg
                &jc -> ch, lx, nv, ml, lq, bs
                %td -> fd, jl
                %xs -> td, fd
                %dg -> jc, rd
                %km -> rg, hm
                %zc -> gj
                %pz -> qh, fd
                %gj -> dl
                %zg -> jc, vn
                %rd -> jc
                %mm -> xx, hm
                &th -> cn
                %gt -> dk, pl
                &hm -> kl, gh, tl, xx, zq
                %bs -> zv
                %cz -> qp
                %tl -> vg
                %hv -> xd
                %ml -> bs, jc
                %bc -> pl
                %xm -> jc, lx
                %vp -> fd, hv
                broadcaster -> kl, ml, xs, jn
                %tx -> xt, hm
                %qf -> bf, hm
                %xt -> zq, hm
                %zv -> xm, jc
                %vg -> hm, mm
                %zz -> fd, pz
                %xd -> fd, lt
                %kl -> hm, tx
                %lx -> nv
                &pl -> cz, gj, sb, sv, jn, zc, dl
                %bj -> fd
                %bf -> hm
                %jn -> pl, sb
                %zm -> lq, jc
                &sv -> cn
                %lt -> mq, fd
                %xx -> km
                %rg -> hm, qf
                %sb -> zc
                %ng -> gt, pl
                %qh -> bj, fd
                %dl -> rj
                %dk -> pl, bc
                %vn -> jc, dg
                &gh -> cn
                %nv -> zm
                &fd -> jl, hv, xs, mq, th
                &ch -> cn
                &cn -> rx
                %zq -> tl
                %rj -> cz, pl
                %jl -> vp
                """;
    }
}