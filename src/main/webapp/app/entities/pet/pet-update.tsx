import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IOwner } from 'app/shared/model/owner.model';
import { getEntities as getOwners } from 'app/entities/owner/owner.reducer';
import { IPet } from 'app/shared/model/pet.model';
import { getEntity, updateEntity, createEntity, reset } from './pet.reducer';

export const PetUpdate = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const owners = useAppSelector(state => state.owner.entities);
  const petEntity = useAppSelector(state => state.pet.entity);
  const loading = useAppSelector(state => state.pet.loading);
  const updating = useAppSelector(state => state.pet.updating);
  const updateSuccess = useAppSelector(state => state.pet.updateSuccess);
  const handleClose = () => {
    props.history.push('/pet');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(props.match.params.id));
    }

    dispatch(getOwners({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...petEntity,
      ...values,
      owner: owners.find(it => it.id.toString() === values.owner.toString()),
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
          ...petEntity,
          owner: petEntity?.owner?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="petSampleApp.pet.home.createOrEditLabel" data-cy="PetCreateUpdateHeading">
            Create or edit a Pet
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="pet-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField label="Name" id="pet-name" name="name" data-cy="name" type="text" />
              <ValidatedField label="Kind" id="pet-kind" name="kind" data-cy="kind" type="text" />
              <ValidatedField label="Age" id="pet-age" name="age" data-cy="age" type="text" />
              <ValidatedField id="pet-owner" name="owner" data-cy="owner" label="Owner" type="select">
                <option value="" key="0" />
                {owners
                  ? owners.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/pet" replace color="info">
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

export default PetUpdate;
