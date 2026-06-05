import { IsNotEmpty, IsOptional, IsString, MaxLength } from 'class-validator';

export class AssistRequestDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(128)
  nodeId: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(2000)
  feeling: string;

  @IsString()
  @IsOptional()
  @MaxLength(4000)
  plainUnderstanding?: string;
}
