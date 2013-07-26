package com.cloudbees.jenkins.plugins.mtslavescloud;

import com.cloudbees.mtslaves.client.VirtualMachineRef;
import hudson.Util;
import hudson.model.Descriptor.FormException;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Slave;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.EphemeralNode;
import hudson.slaves.NodeProperty;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * {@link Slave} for {@link MansionCloud}
 *
 * @author Kohsuke Kawaguchi
 */
public class MansionSlave extends AbstractCloudSlave implements EphemeralNode {
    private final VirtualMachineRef vm;
    private final SlaveTemplate template;

    public MansionSlave(VirtualMachineRef vm, SlaveTemplate template, Label label, ComputerLauncher launcher) throws FormException, IOException {
        super(
                Util.getDigestOf(vm.getId()).substring(0,8),
                "Virtual machine provisioned from "+vm.url,
                "/scratch/jenkins", // TODO:
                1,
                Mode.NORMAL,
                label.getDisplayName(),
                launcher,
                new CloudSlaveRetentionstrategy(),
                Collections.<NodeProperty<?>>emptyList());
        this.vm = vm;
        this.template = template;

        // suspend retention strategy until we do the initial launch
        this.holdOffLaunchUntilSave = true;
    }

    protected void cancelHoldOff() {
        // resume the retention strategy work that we've suspended in the constructor
        holdOffLaunchUntilSave = false;
    }

    @Override
    public MansionComputer createComputer() {
        return new MansionComputer(this);
    }

    public Node asNode() {
        return this;
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
        FileSystemClan clan = template.loadClan();
        clan.update(vm.getState());
        vm.dispose();
        listener.getLogger().println("Disposed " + vm.url);
    }

    private static final Logger LOGGER = Logger.getLogger(MansionSlave.class.getName());
}
