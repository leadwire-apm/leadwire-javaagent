/**
 */
package kieker.analysisteetime.model.analysismodel.deployment.impl;

import java.lang.reflect.InvocationTargetException;
import kieker.analysisteetime.model.analysismodel.assembly.AssemblyComponent;
import kieker.analysisteetime.model.analysismodel.deployment.DeployedComponent;
import kieker.analysisteetime.model.analysismodel.deployment.DeployedOperation;
import kieker.analysisteetime.model.analysismodel.deployment.DeploymentContext;
import kieker.analysisteetime.model.analysismodel.deployment.DeploymentPackage;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Deployed Component</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link kieker.analysisteetime.model.analysismodel.deployment.impl.DeployedComponentImpl#getAssemblyOperation <em>Assembly Operation</em>}</li>
 *   <li>{@link kieker.analysisteetime.model.analysismodel.deployment.impl.DeployedComponentImpl#getContainedOperations <em>Contained Operations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DeployedComponentImpl extends MinimalEObjectImpl.Container implements DeployedComponent {
	/**
	 * The cached value of the '{@link #getAssemblyOperation() <em>Assembly Operation</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssemblyOperation()
	 * @generated
	 * @ordered
	 */
	protected AssemblyComponent assemblyOperation;

	/**
	 * The cached value of the '{@link #getContainedOperations() <em>Contained Operations</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContainedOperations()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, DeployedOperation> containedOperations;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DeployedComponentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.DEPLOYED_COMPONENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AssemblyComponent getAssemblyOperation() {
		if (assemblyOperation != null && assemblyOperation.eIsProxy()) {
			InternalEObject oldAssemblyOperation = (InternalEObject)assemblyOperation;
			assemblyOperation = (AssemblyComponent)eResolveProxy(oldAssemblyOperation);
			if (assemblyOperation != oldAssemblyOperation) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DeploymentPackage.DEPLOYED_COMPONENT__ASSEMBLY_OPERATION, oldAssemblyOperation, assemblyOperation));
			}
		}
		return assemblyOperation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AssemblyComponent basicGetAssemblyOperation() {
		return assemblyOperation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAssemblyOperation(AssemblyComponent newAssemblyOperation) {
		AssemblyComponent oldAssemblyOperation = assemblyOperation;
		assemblyOperation = newAssemblyOperation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.DEPLOYED_COMPONENT__ASSEMBLY_OPERATION, oldAssemblyOperation, assemblyOperation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, DeployedOperation> getContainedOperations() {
		if (containedOperations == null) {
			containedOperations = new EcoreEMap<String,DeployedOperation>(DeploymentPackage.Literals.ESTRING_TO_DEPLOYED_OPERATION_MAP_ENTRY, EStringToDeployedOperationMapEntryImpl.class, this, DeploymentPackage.DEPLOYED_COMPONENT__CONTAINED_OPERATIONS);
		}
		return containedOperations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DeploymentContext getDeploymentContext() {
		org.eclipse.emf.ecore.EObject container = this.eContainer();
		if (container != null) {
			org.eclipse.emf.ecore.EObject containerContainer = container.eContainer();
			if (containerContainer != null) {
				return (DeploymentContext) containerContainer ;
			}
		}
		return null;
		
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DeploymentPackage.DEPLOYED_COMPONENT__CONTAINED_OPERATIONS:
				return ((InternalEList<?>)getContainedOperations()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.DEPLOYED_COMPONENT__ASSEMBLY_OPERATION:
				if (resolve) return getAssemblyOperation();
				return basicGetAssemblyOperation();
			case DeploymentPackage.DEPLOYED_COMPONENT__CONTAINED_OPERATIONS:
				if (coreType) return getContainedOperations();
				else return getContainedOperations().map();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DeploymentPackage.DEPLOYED_COMPONENT__ASSEMBLY_OPERATION:
				setAssemblyOperation((AssemblyComponent)newValue);
				return;
			case DeploymentPackage.DEPLOYED_COMPONENT__CONTAINED_OPERATIONS:
				((EStructuralFeature.Setting)getContainedOperations()).set(newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case DeploymentPackage.DEPLOYED_COMPONENT__ASSEMBLY_OPERATION:
				setAssemblyOperation((AssemblyComponent)null);
				return;
			case DeploymentPackage.DEPLOYED_COMPONENT__CONTAINED_OPERATIONS:
				getContainedOperations().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case DeploymentPackage.DEPLOYED_COMPONENT__ASSEMBLY_OPERATION:
				return assemblyOperation != null;
			case DeploymentPackage.DEPLOYED_COMPONENT__CONTAINED_OPERATIONS:
				return containedOperations != null && !containedOperations.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case DeploymentPackage.DEPLOYED_COMPONENT___GET_DEPLOYMENT_CONTEXT:
				return getDeploymentContext();
		}
		return super.eInvoke(operationID, arguments);
	}

} //DeployedComponentImpl
