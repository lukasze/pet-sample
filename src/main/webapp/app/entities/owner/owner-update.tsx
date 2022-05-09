import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, FormText, UncontrolledTooltip } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IOwner } from 'app/shared/model/owner.model';
import { getEntity, updateEntity, createEntity, reset } from './owner.reducer';

export const OwnerUpdate = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const ownerEntity = useAppSelector(state => state.owner.entity);
  const loading = useAppSelector(state => state.owner.loading);
  const updating = useAppSelector(state => state.owner.updating);
  const updateSuccess = useAppSelector(state => state.owner.updateSuccess);
  const handleClose = () => {
    props.history.push('/owner');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(props.match.params.id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...ownerEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...ownerEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="petSampleApp.owner.home.createOrEditLabel" data-cy="OwnerCreateUpdateHeading">
            Create or edit a Owner
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="owner-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField label="First Name" id="owner-firstName" name="firstName" data-cy="firstName" type="text" />
              <UncontrolledTooltip target="firstNameLabel">The firstname attribute.</UncontrolledTooltip>
              <ValidatedField label="Last Name" id="owner-lastName" name="lastName" data-cy="lastName" type="text" />
              <ValidatedField label="Email" id="owner-email" name="email" data-cy="email" type="text" />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/owner" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default OwnerUpdate;
