from typing import Optional

from pydantic import BaseModel, Field, model_validator

from app.models.user import FitnessGoal, FitnessLevel


class OnboardingRequest(BaseModel):
    weight: float = Field(gt=0, description="Вес в кг")
    height: float = Field(gt=0, description="Рост в см")
    goal: FitnessGoal
    level: FitnessLevel
    gender: Optional[str] = None
    age: Optional[int] = Field(default=None, gt=0, description="Возраст")
    injuries: list[str] = Field(default_factory=list)
    equipment: list[str] = Field(default_factory=list)
    medical_disclaimer_accepted: bool = Field(default=False)

    @model_validator(mode="after")
    def validate_medical_disclaimer(self):
        if not self.medical_disclaimer_accepted:
            raise ValueError("Необходимо принять медицинский дисклеймер")
        return self


class OnboardingResponse(BaseModel):
    message: str = "Onboarding completed"
    onboarding_completed: bool = True
