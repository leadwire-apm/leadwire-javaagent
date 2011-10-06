/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.tools.traceAnalysis.plugins.visualization.dependencyGraph;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import kieker.analysis.plugin.configuration.AbstractInputPort;
import kieker.analysis.plugin.configuration.IInputPort;
import kieker.tools.traceAnalysis.plugins.visualization.util.dot.DotFactory;
import kieker.tools.traceAnalysis.systemModel.AbstractMessage;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import kieker.tools.traceAnalysis.systemModel.Operation;
import kieker.tools.traceAnalysis.systemModel.Signature;
import kieker.tools.traceAnalysis.systemModel.SynchronousReplyMessage;
import kieker.tools.traceAnalysis.systemModel.repository.AbstractSystemSubRepository;
import kieker.tools.traceAnalysis.systemModel.repository.AssemblyComponentOperationPairFactory;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import kieker.tools.traceAnalysis.systemModel.util.AssemblyComponentOperationPair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Refactored copy from LogAnalysis-legacy tool
 * 
 * @author Andre van Hoorn, Lena St&ouml;ver, Matthias Rohr,
 */
public class OperationDependencyGraphPluginAssembly extends AbstractDependencyGraphPlugin<AssemblyComponentOperationPair> {

	private static final Log LOG = LogFactory.getLog(OperationDependencyGraphPluginAssembly.class);
	private final static String COMPONENT_NODE_ID_PREFIX = "component_";
	private final AssemblyComponentOperationPairFactory pairFactory;
	private final File dotOutputFile;
	private final boolean includeWeights;
	private final boolean shortLabels;
	private final boolean includeSelfLoops;

	public OperationDependencyGraphPluginAssembly(final String name, final SystemModelRepository systemEntityFactory, final File dotOutputFile,
			final boolean includeWeights, final boolean shortLabels, final boolean includeSelfLoops) {
		super(name, systemEntityFactory, new DependencyGraph<AssemblyComponentOperationPair>(AbstractSystemSubRepository.ROOT_ELEMENT_ID,
				new AssemblyComponentOperationPair(AbstractSystemSubRepository.ROOT_ELEMENT_ID, systemEntityFactory.getOperationFactory().rootOperation,
						systemEntityFactory.getAssemblyFactory().rootAssemblyComponent)));
		this.pairFactory = new AssemblyComponentOperationPairFactory(systemEntityFactory);
		this.dotOutputFile = dotOutputFile;
		this.includeWeights = includeWeights;
		this.shortLabels = shortLabels;
		this.includeSelfLoops = includeSelfLoops;
	}

	private String componentNodeLabel(final AssemblyComponent component, final boolean shortLabels) {
		final String assemblyComponentName = component.getName();
		final String componentTypePackagePrefx = component.getType().getPackageName();
		final String componentTypeIdentifier = component.getType().getTypeName();

		final StringBuilder strBuild = new StringBuilder(AbstractDependencyGraphPlugin.STEREOTYPE_ASSEMBLY_COMPONENT + "\\n");
		strBuild.append(assemblyComponentName).append(":");
		if (!shortLabels) {
			strBuild.append(componentTypePackagePrefx).append(".");
		} else {
			strBuild.append("..");
		}
		strBuild.append(componentTypeIdentifier);
		return strBuild.toString();
	}

	@Override
	protected void dotEdges(final Collection<DependencyGraphNode<AssemblyComponentOperationPair>> nodes, final PrintStream ps, final boolean shortLabels) {

		/* Component ID x contained operations */
		final Map<Integer, Collection<DependencyGraphNode<AssemblyComponentOperationPair>>> componentId2pairMapping = new Hashtable<Integer, Collection<DependencyGraphNode<AssemblyComponentOperationPair>>>();

		// Derive component / operation hierarchy
		for (final DependencyGraphNode<AssemblyComponentOperationPair> pairNode : nodes) {
			final AssemblyComponent curComponent = pairNode.getEntity().getAssemblyComponent();
			final int componentId = curComponent.getId();

			Collection<DependencyGraphNode<AssemblyComponentOperationPair>> containedPairs = componentId2pairMapping.get(componentId);
			if (containedPairs == null) {
				// component not yet registered
				containedPairs = new ArrayList<DependencyGraphNode<AssemblyComponentOperationPair>>();
				componentId2pairMapping.put(componentId, containedPairs);
			}
			containedPairs.add(pairNode);
		}

		final AssemblyComponent rootComponent = this.getSystemEntityFactory().getAssemblyFactory().rootAssemblyComponent;
		final int rootComponentId = rootComponent.getId();
		final StringBuilder strBuild = new StringBuilder();
		for (final Entry<Integer, Collection<DependencyGraphNode<AssemblyComponentOperationPair>>> componentOperationEntry : componentId2pairMapping.entrySet()) {
			final int curComponentId = componentOperationEntry.getKey();
			final AssemblyComponent curComponent = this.getSystemEntityFactory().getAssemblyFactory().lookupAssemblyComponentById(curComponentId);

			if (curComponentId == rootComponentId) {
				strBuild.append(DotFactory.createNode("", this.getNodeId(this.dependencyGraph.getRootNode()), "$", DotFactory.DOT_SHAPE_NONE, null, // style
						null, // framecolor
						null, // fillcolor
						null, // fontcolor
						DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
						null, // imagefilename
						null // misc
						));
			} else {
				strBuild.append(DotFactory.createCluster("", OperationDependencyGraphPluginAssembly.COMPONENT_NODE_ID_PREFIX + curComponentId,
						this.componentNodeLabel(curComponent, this.shortLabels), DotFactory.DOT_SHAPE_BOX, // shape
						DotFactory.DOT_STYLE_FILLED, // style
						null, // framecolor
						DotFactory.DOT_FILLCOLOR_WHITE, // fillcolor
						null, // fontcolor
						DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
						null)); // misc
				for (final DependencyGraphNode<AssemblyComponentOperationPair> curPair : componentOperationEntry.getValue()) {
					final Signature sig = curPair.getEntity().getOperation().getSignature();
					final StringBuilder opLabel = new StringBuilder(sig.getName());
					opLabel.append("(");
					final String[] paramList = sig.getParamTypeList();
					if ((paramList != null) && (paramList.length > 0)) {
						opLabel.append("..");
					}
					opLabel.append(")");
					strBuild.append(DotFactory.createNode("", this.getNodeId(curPair), opLabel.toString(), DotFactory.DOT_SHAPE_OVAL, DotFactory.DOT_STYLE_FILLED, // style
							null, // framecolor
							DotFactory.DOT_FILLCOLOR_WHITE, // fillcolor
							null, // fontcolor
							DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
							null, // imagefilename
							null // misc
							));
				}
				strBuild.append("}\n");
			}
		}
		ps.println(strBuild.toString());
	}

	@Override
	public boolean execute() {
		return true; // no need to do anything here
	}

	/**
	 * Saves the dependency graph to the dot file if error is not true.
	 * 
	 * @param error
	 */
	@Override
	public void terminate(final boolean error) {
		if (!error) {
			try {
				this.saveToDotFile(this.dotOutputFile.getCanonicalPath(), this.includeWeights, this.shortLabels, this.includeSelfLoops);
			} catch (final IOException ex) {
				OperationDependencyGraphPluginAssembly.LOG.error("IOException", ex);
			}
		}
	}

	private final IInputPort<MessageTrace> messageTraceInputPort = new AbstractInputPort<MessageTrace>("Message traces") {

		@Override
		public void newEvent(final MessageTrace t) {
			for (final AbstractMessage m : t.getSequenceAsVector()) {
				if (m instanceof SynchronousReplyMessage) {
					continue;
				}
				final AssemblyComponent senderComponent = m.getSendingExecution().getAllocationComponent().getAssemblyComponent();
				final AssemblyComponent receiverComponent = m.getReceivingExecution().getAllocationComponent().getAssemblyComponent();
				final int rootOperationId = OperationDependencyGraphPluginAssembly.this.getSystemEntityFactory().getOperationFactory().rootOperation.getId();
				final Operation senderOperation = m.getSendingExecution().getOperation();
				final Operation receiverOperation = m.getReceivingExecution().getOperation();
				/* The following two get-calls to the factory return s.th. in either case */
				final AssemblyComponentOperationPair senderPair = (senderOperation.getId() == rootOperationId) ? OperationDependencyGraphPluginAssembly.this.dependencyGraph // NOCS
						.getRootNode().getEntity()
						: OperationDependencyGraphPluginAssembly.this.pairFactory.getPairInstanceByPair(senderComponent, senderOperation);
				final AssemblyComponentOperationPair receiverPair = (receiverOperation.getId() == rootOperationId) ? OperationDependencyGraphPluginAssembly.this.dependencyGraph // NOCS
						.getRootNode().getEntity()
						: OperationDependencyGraphPluginAssembly.this.pairFactory.getPairInstanceByPair(receiverComponent, receiverOperation);

				DependencyGraphNode<AssemblyComponentOperationPair> senderNode = OperationDependencyGraphPluginAssembly.this.dependencyGraph.getNode(senderPair
						.getId());
				DependencyGraphNode<AssemblyComponentOperationPair> receiverNode = OperationDependencyGraphPluginAssembly.this.dependencyGraph.getNode(receiverPair
						.getId());
				if (senderNode == null) {
					senderNode = new DependencyGraphNode<AssemblyComponentOperationPair>(senderPair.getId(), senderPair);
					OperationDependencyGraphPluginAssembly.this.dependencyGraph.addNode(senderNode.getId(), senderNode);
				}
				if (receiverNode == null) {
					receiverNode = new DependencyGraphNode<AssemblyComponentOperationPair>(receiverPair.getId(), receiverPair);
					OperationDependencyGraphPluginAssembly.this.dependencyGraph.addNode(receiverNode.getId(), receiverNode);
				}
				senderNode.addOutgoingDependency(receiverNode);
				receiverNode.addIncomingDependency(senderNode);
			}
			OperationDependencyGraphPluginAssembly.this.reportSuccess(t.getTraceId());
		}
	};

	@Override
	public IInputPort<MessageTrace> getMessageTraceInputPort() {
		return this.messageTraceInputPort;
	}
}
